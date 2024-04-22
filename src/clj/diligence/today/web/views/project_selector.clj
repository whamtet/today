(ns diligence.today.web.views.project-selector
    (:require
      [diligence.today.web.controllers.iam :as iam]
      [diligence.today.web.controllers.project :as project]
      [diligence.today.web.htmx :refer [defcomponent-user]]
      [diligence.today.web.views.common :as common]
      [diligence.today.web.views.components :as components]
      [diligence.today.web.views.dropdown :as dropdown]
      [simpleui.response :as response]))

(defn assoc-session [{:keys [session]} k v]
  (assoc response/hx-refresh :session (assoc session k v)))
(defn dissoc-session [{:keys [session]} k]
  (assoc response/hx-refresh :session (dissoc session k)))

(defn- uniqueness-violation? [e]
  (-> e str (.contains "SQLITE_CONSTRAINT_UNIQUE")))

(defcomponent-user ^:endpoint project-selector [req command project-name]
  (case command
        "new" (iam/when-authorized
               (try
                 (project/create-project req project-name)
                 (assoc-session req :project-name project-name)
                 (catch clojure.lang.ExceptionInfo e
                   (if (uniqueness-violation? e)
                     [:div#unique-warning.my-3 (components/warning "Project name in use.")]
                     (throw e)))))
        "start" (iam/when-authorized
                 (assoc-session req :project-name project-name))
        "unselect" (iam/when-authorized
                    (dissoc-session req :project-name))
        [:div {:_ "on click add .hidden to .drop"}
         ;; header row
         (common/header-row first_name true)
         [:div {:class "w-1/2 border rounded-lg mx-auto"}
          ;; creation form
          [:form {:class "flex"
                  :hx-post "project-selector:new"
                  :hx-target "#unique-warning"}
           [:input {:class "p-2 w-full"
                    :name "project-name"
                    :placeholder "Create project..."
                    :required true}]
           [:input {:class "bg-clj-blue p-1.5 rounded-lg text-white w-24"
                    :type "submit"
                    :value "Create"}]]
          [:div#unique-warning]
          ;; project selector
          [:div.text-gray-500
           (for [{:keys [name]} (project/get-projects req)]
             [:div {:class "p-3 hover:bg-slate-100 cursor-pointer"
                    :hx-post "project-selector:start"
                    :hx-vals {:project-name name}}
              name])]]]))
