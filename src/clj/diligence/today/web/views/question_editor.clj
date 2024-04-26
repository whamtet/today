(ns diligence.today.web.views.question-editor
    (:require
      [diligence.today.web.controllers.iam :as iam]
      [diligence.today.web.controllers.project :as project]
      [diligence.today.web.controllers.question :as question]
      [diligence.today.web.htmx :refer [page-htmx defcomponent defcomponent-user]]
      [diligence.today.web.views.common :as common]
      [diligence.today.web.views.components :as components]
      [diligence.today.web.views.dropdown :as dropdown]
      [diligence.today.web.views.icons :as icons]
      [simpleui.core :as simpleui]
      [simpleui.response :as response]))

(defcomponent-user ^:endpoint question-editor [req]
  (let [questions (question/get-questions req project_id)
        {project-name :name} (project/get-project-by-id req project_id)]
    [:div {:_ "on click add .hidden to .drop"}
     ;; header row
     [:div {:class "flex justify-center"}
      [:a.absolute.left-1.top-1 {:href "/"}
       [:img.w-16.m-2 {:src "/icon.png"}]]
      (common/main-dropdown first_name)]
     [:div {:class "w-3/4 border rounded-lg mx-auto mt-16"}
      "fuck yeah!"]]))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (if (-> req :session :user_id)
       (page-htmx
        {:hyperscript? true}
        (-> req (assoc :query-fn query-fn) question-editor))
       (response/redirect "/")))))
