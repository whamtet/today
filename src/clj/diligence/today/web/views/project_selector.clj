(ns diligence.today.web.views.project-selector
    (:require
      [diligence.today.web.controllers.iam :as iam]
      [diligence.today.web.controllers.project :as project]
      [diligence.today.web.htmx :refer [defcomponent-user]]
      [diligence.today.web.views.components :as components]
      [diligence.today.web.views.dropdown :as dropdown]
      [simpleui.response :as response]))

(defn assoc-session [{:keys [session]} k v]
  (assoc response/hx-refresh :session (assoc session k v)))

(defn- uniqueness-violation? [e]
  (-> e str (.contains "SQLITE_CONSTRAINT_UNIQUE")))

(defn main-dropdown [user_name]
  [:div.absolute.top-1.right-1.flex.items-center
   (dropdown/dropdown
    (str "Welcome " user_name)
    [[:a {:href ""}
      [:div.p-2 {:hx-post "/api/logout"}
       "Logout"]]])])

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
        [:div {:_ "on click add .hidden to .drop"}
         ;; header row
         [:div
          [:a.inline-block {:href "/"}
           [:img.w-16.m-2 {:src "/icon.png"}]]
          (main-dropdown first_name)]
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
