(ns diligence.today.web.views.settings
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

(defcomponent ^:endpoint question-ro [req question_id question]
  (if (simpleui/delete? req)
    (iam/when-authorized
     (question/delete-question req question_id)
     "")
    [:form {:class "flex items-center my-2"
            :hx-delete "question-ro"
            :hx-confirm "Permanently delete?"}
     (components/hiddensm question_id)
     [:div.min-w-72.mx-2 question]
     [:input {:class "bg-clj-blue p-1.5 rounded-lg text-white w-24"
              :type "submit"
              :value "Delete"}]]))

(defcomponent-user ^:endpoint question-maker [req]
  (let [questions (question/get-questions-all req)]
    [:div {:class "mt-12"
           :_ "on click add .hidden to .drop"}
     ;; header row
     [:div
      [:a.absolute.left-1.top-1 {:href "/"}
       [:img.w-16.m-2 {:src "/icon.png"}]]
      (common/main-dropdown first_name)]
     [:div {:class "w-3/4 border rounded-lg mx-auto"}
      (for [{:keys [question_id question]} questions]
        (question-ro req question_id question))]]))

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
