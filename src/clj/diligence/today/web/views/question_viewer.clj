(ns diligence.today.web.views.question-viewer
    (:require
      [diligence.today.web.controllers.project :as project]
      [diligence.today.web.controllers.question :as question]
      [diligence.today.web.htmx :refer [page-htmx defcomponent defcomponent-user]]
      [diligence.today.web.views.common :as common]
      [simpleui.core :as simpleui]
      [simpleui.response :as response]))

(defn project-ro [project-name]
  [:form {:class "flex items-center"}
   [:div.my-6.mr-4.text-gray-500.text-4xl project-name]])

(defcomponent-user question-viewer [req]
  (let [questions (question/get-questions req project_id)
        {project-name :name} (project/get-project-by-id req project_id)]
    [:div {:_ "on click add .hidden to .drop"}
     ;; header row
     [:div {:class "flex justify-center"}
      [:a.absolute.left-1.top-1 {:href "/"}
       [:img.w-16.m-2 {:src "/icon.png"}]]
      (project-ro project-name)
      (common/main-dropdown first_name project_id)]
     [:div {:class "w-3/4 border rounded-md mx-auto p-1"}
      (if (empty? questions)
        [:div.text-gray-500.m-2 "Ask your admin to create questions"]
        (for [[{:keys [section]} questions] questions]
          [:div
           [:div.text-xl section]
           (for [{:keys [question_id question]} questions]
             [:div.text-blue-800.m-2
              [:a {:href (str "question/" question_id)}
               question]])]))]]))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:hyperscript? true}
      (-> req (assoc :query-fn query-fn) question-viewer)))))
