(ns diligence.today.web.views.question-editor
    (:require
      [diligence.today.env :refer [dev?]]
      [diligence.today.web.controllers.file :as file]
      [diligence.today.web.controllers.fragment :as fragment]
      [diligence.today.web.controllers.iam :as iam]
      [diligence.today.web.controllers.question :as question]
      [diligence.today.web.htmx :refer [page-htmx defcomponent defcomponent-user]]
      [diligence.today.web.views.common :as common :refer [href-viewer]]
      [diligence.today.web.views.components :as components]
      [diligence.today.web.views.dropdown :as dropdown]
      [diligence.today.web.views.icons :as icons]
      [diligence.today.web.views.soft-links :as soft-links]
      [simpleui.core :as simpleui]
      [simpleui.response :as response]))

(defn file-selector [req question_id]
  (list
   [:div.mb-2 (components/button-label "add-file" "Add File")
    [:input#add-file {:class "hidden"
                      :type "file"
                      :name "file"
                      :accept "application/pdf"
                      :hx-post "question-editor"
                      :hx-encoding "multipart/form-data"}]]
   [:div.flex.items-center
    (for [{:keys [file_id]} (file/get-files req question_id)]
      [:a {:href (href-viewer question_id)
           :target "_blank"}
       [:img {:class "w-64"
              :src (str "/api/thumbnail/" file_id)}]])]))

(defcomponent ^:endpoint fragment-selector [req fragment_id command]
  (case command
        "del"
        (iam/when-authorized
         (fragment/delete-fragment req fragment_id)
         "")
        [:div.mb-2
         [:h3 "Hard Links"]
         (for [{:keys [fragment_id fragment page]} (fragment/get-fragments req (:question_id path-params))]
           [:div.flex.mt-2 {:hx-target "this"}
            [:span {:class "w-20 text-gray-500 text-center cursor-pointer"}
             (format "(pp %s)" page)]
            [:a {:href (href-viewer (:question_id path-params) page)
                 :target "_blank"}
             [:div {:class "w-96 truncate"} fragment]]
            [:div {:class "text-gray-500 cursor-pointer"
                   :hx-delete "fragment-selector:del"
                   :hx-vals {:fragment_id fragment_id}}
             icons/trash]])]))

(defcomponent-user ^:endpoint question-editor [req file]
  (if (simpleui/post? req)
    (iam/when-authorized
     (file/copy-file req (:question_id path-params) file)
     response/hx-refresh)
    (let [{question-name :question} (question/get-question req (:question_id path-params))]
      [:div {:_ "on click add .hidden to .drop"}
       ;; header row
       [:div {:class "flex justify-center"}
        [:a.absolute.left-1.top-1 {:href "/"}
         [:img.w-16.m-2 {:src "/icon.png"}]]
        [:div.my-6.mr-4.text-gray-500.text-4xl question-name]
        (common/main-dropdown first_name)]
       [:div {:class "w-3/4 border rounded-lg mx-auto p-2"}
        (file-selector req (:question_id path-params))
        (fragment-selector req)
        (soft-links/soft-links req)]])))

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
