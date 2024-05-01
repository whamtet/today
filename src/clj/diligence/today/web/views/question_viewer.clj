(ns diligence.today.web.views.question-viewer
    (:require
      [diligence.today.web.controllers.question :as question]
      [diligence.today.web.htmx :refer [page-htmx defcomponent defcomponent-user]]
      [diligence.today.web.views.common :as common]
      [simpleui.core :as simpleui]
      [simpleui.response :as response]))

(defcomponent-user ^:endpoint question-maker [req]
  (let [questions (question/get-questions req project_id)]
    [:div {:class "mt-12"
           :_ "on click add .hidden to .drop"}
     ;; header row
     [:div
      [:a.absolute.left-1.top-1 {:href "/"}
       [:img.w-16.m-2 {:src "/icon.png"}]]
      (common/main-dropdown first_name project_id)]
     [:div {:class "w-3/4 border rounded-lg mx-auto"}
      (if (empty? questions)
        [:div.text-gray-500.m-2 "Ask your admin to create questions"]
        (for [{:keys [question_id question]} questions]
          [:div.text-blue-800.m-2
           [:a {:href (str "question/" question_id)}
            question]]))]]))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (if (-> req :session :user_id)
       (page-htmx
        {:hyperscript? true}
        (-> req (assoc :query-fn query-fn) question-maker))
       (response/redirect "/")))))
