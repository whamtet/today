(ns diligence.today.web.views.answer
    (:require
      [diligence.today.web.controllers.iam :as iam]
      [diligence.today.web.controllers.question :as question]
      [diligence.today.web.htmx :refer [page-htmx defcomponent defcomponent-user]]
      [diligence.today.web.views.common :as common]
      [diligence.today.web.views.editor :as editor]
      [simpleui.core :as simpleui]
      [simpleui.response :as response]))

(defcomponent-user answer [req file]
  (let [{question-name :question} (question/get-question req (:question_id path-params))]
    [:div {:_ "on click add .hidden to .drop"}
     ;; header row
     [:div {:class "flex justify-center"}
      [:a.absolute.left-1.top-1 {:href "/"}
       [:img.w-16.m-2 {:src "/icon.png"}]]
      [:div.my-6.mr-4.text-gray-500.text-4xl question-name]
      (common/main-dropdown first_name project_id)]
     [:div {:class "w-3/4 border rounded-lg mx-auto p-2"}
      (editor/editor req)]]))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (if (-> req :session :user_id)
       (page-htmx
        {:hyperscript? true
         :js ["/editor.js"]}
        (-> req (assoc :query-fn query-fn) answer))
       (response/redirect "/")))))
