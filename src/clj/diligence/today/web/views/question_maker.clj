(ns diligence.today.web.views.question-maker
    (:require
      [diligence.today.web.controllers.iam :as iam]
      [diligence.today.web.controllers.project :as project]
      [diligence.today.web.controllers.question :as question]
      [diligence.today.web.htmx :refer [page-htmx defcomponent defcomponent-user]]
      [diligence.today.web.views.common :as common]
      [diligence.today.web.views.components :as components]
      [diligence.today.web.views.dropdown :as dropdown]
      [simpleui.core :as simpleui]
      [simpleui.response :as response]))

(defcomponent ^:endpoint question-edit [req ^:long question_id question]
  [:div
   [:input.full {:class "p-2"
                 :name "questions"
                 :value question}]])

(defcomponent-user ^:endpoint question-maker [req ^:array questions]
  (let [questions (question/get-questions req project_id)
        {project-name :name} (project/get-project-by-id req project_id)]
    [:form {:hx-post "question-maker"
            :_ "on click add .hidden to .drop"}
     ;; header row
     [:div
      [:a.absolute.left-1.top-1 {:href "/"}
       [:img.w-16.m-2 {:src "/icon.png"}]]
      [:h2.my-6.text-center.text-gray-500 project-name]
      (common/main-dropdown first_name false)]
     [:div {:class "w-3/4 border rounded-lg mx-auto"}
      (for [{:keys [question_id question]} questions]
        (question-edit req question_id question))]]))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:hyperscript? true}
      (-> req (assoc :query-fn query-fn) question-maker)))))
