(ns diligence.today.web.views.question-editor
    (:require
      [diligence.today.web.controllers.file :as file]
      [diligence.today.web.controllers.iam :as iam]
      [diligence.today.web.controllers.question :as question]
      [diligence.today.web.htmx :refer [page-htmx defcomponent defcomponent-user]]
      [diligence.today.web.views.common :as common]
      [diligence.today.web.views.components :as components]
      [diligence.today.web.views.dropdown :as dropdown]
      [diligence.today.web.views.icons :as icons]
      [simpleui.core :as simpleui]
      [simpleui.response :as response]))

(defcomponent-user ^:endpoint question-editor [req file]
  (if (simpleui/post? req)
    (iam/when-authorized
     (file/copy-file req question_id file)
     response/hx-refresh)
    (let [{question-name :question} (question/get-question req question_id)]
      [:div {:_ "on click add .hidden to .drop"}
       ;; header row
       [:div {:class "flex justify-center"}
        [:a.absolute.left-1.top-1 {:href "/"}
         [:img.w-16.m-2 {:src "/icon.png"}]]
        [:div.my-6.mr-4.text-gray-500.text-4xl question-name]
        (common/main-dropdown first_name)]
       [:div {:class "w-3/4 border rounded-lg mx-auto p-2"}
        [:div.mb-2 (components/button-label "add-file" "Add File")
         [:input#add-file {:class "hidden"
                           :type "file"
                           :name "file"
                           :accept "application/pdf"
                           :hx-post "question-editor"
                           :hx-encoding "multipart/form-data"}]]
        [:div.flex.items-center
         (for [{:keys [file_id]} (file/get-files req question_id)]
           [:a {:href (str "http://localhost:8888/web/viewer.html?question_id=" question_id)
                :target "_blank"}
            [:img {:class "w-64"
                   :src (str "/api/thumbnail/" file_id)}]])]]])))

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
