(ns diligence.today.web.views.project-selector
    (:require
      [diligence.today.util :as util]
      [diligence.today.web.controllers.iam :as iam]
      [diligence.today.web.controllers.project :as project]
      [diligence.today.web.htmx :refer [defcomponent-user]]
      [diligence.today.web.views.common :as common]
      [diligence.today.web.views.components :as components]
      [diligence.today.web.views.dropdown :as dropdown]
      [simpleui.response :as response]))

(defcomponent-user ^:endpoint project-selector [req command new-project-name]
  (case command
        "new" (iam/when-authorized
               (try
                 (let [project_id (project/create-project req new-project-name)]
                   (response/hx-redirect (format "/project/%s/" project_id)))
                 (catch clojure.lang.ExceptionInfo e
                   (if (util/uniqueness-violation? e)
                     [:div#unique-warning.my-3 (components/warning "Project name in use.")]
                     (throw e)))))
        [:div {:_ "on click add .hidden to .drop"}
         ;; header row
         [:div
          [:a.inline-block {:href "/"}
           [:img.w-16.m-2 {:src "/icon.png"}]]
          (common/main-dropdown first_name session)]
         [:div {:class "w-1/2 border rounded-lg mx-auto"}
          ;; creation form
          [:form {:class "flex"
                  :hx-post "project-selector:new"
                  :hx-target "#unique-warning"}
           [:input {:class "p-2 w-full"
                    :name "new-project-name"
                    :placeholder "Create project..."
                    :required true}]
           [:input {:class "bg-clj-blue p-1.5 rounded-lg text-white w-24"
                    :type "submit"
                    :value "Create"}]]
          [:div#unique-warning]
          ;; project selector
          [:div.text-gray-500
           (for [{:keys [name project_id]} (project/get-projects req)]
             [:a {:href (str "/project/" project_id)}
              [:div {:class "p-3 hover:bg-slate-100 cursor-pointer"}
               name]])]]]))
