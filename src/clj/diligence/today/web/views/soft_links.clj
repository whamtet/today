(ns diligence.today.web.views.soft-links
    (:require
      [diligence.today.web.controllers.iam :as iam]
      [diligence.today.web.controllers.question :as question]
      [diligence.today.web.controllers.soft-link :as soft-link]
      [diligence.today.web.htmx :refer [defcomponent]]
      [diligence.today.web.services.grep :as grep]
      [diligence.today.web.views.common :as common]
      [diligence.today.web.views.components :as components]
      [diligence.today.web.views.dropdown :as dropdown]
      [diligence.today.web.views.icons :as icons]
      [simpleui.core :as simpleui]
      [simpleui.response :as response]))

(defcomponent ^:endpoint new-soft-link [req new-q command]
  #_
  (case command
        "search"
        (iam/when-authorized
         (let [{:keys [filename]} (soft-link/get-file req (:question_id path-params))]
           [:div#link-preview {:class "text-gray-600 p-2"}
            (for [[page result] (grep/grep project_id new-q filename)]
              [:a {:href (common/href-viewer (:question_id path-params) page)
                   :target "_blank"}
               [:div.text-gray-500 page ": " result]])]))
        "create"
        (iam/when-authorized
         (let [{:keys [filename]} (soft-link/get-file req (:question_id path-params))]
           (when (not-empty (grep/grep project_id new-q filename))
                 (soft-link/insert-soft-link req (:question_id path-params) new-q)
                 response/hx-refresh)))
        [:div
         [:div.flex
          [:input {:class "w-96 p-2 link-input"
                   :name "new-q"
                   :placeholder "New Soft Link..."
                   :hx-get "new-soft-link:search"
                   :hx-trigger "keyup changed delay:0.5s"
                   :hx-target "#link-preview"}]
          [:div {:class "ml-2"
                 :hx-post "new-soft-link:create"
                 :hx-include ".link-input"}
           (components/button "Add Link")]]
         [:div#link-preview]]))

(defcomponent ^:endpoint soft-links [req command q]
  #_
  (let [{:keys [file_id filename]} (soft-link/get-file req (:question_id path-params))]
    (case command
          "del"
          (iam/when-authorized
           (soft-link/delete-soft-link req (:question_id path-params) q)
           response/hx-refresh)
          [:div
           [:h3.mb-3 "Soft Links"]
           (for [q (soft-link/get-soft-links req file_id)]
             (let [results (grep/grep project_id q filename)]
               [:div [:h4.flex q [:span {:class "text-gray-500 ml-2 cursor-pointer"
                                         :hx-delete "soft-links:del"
                                         :hx-vals {:q q}}
                                  icons/trash]]
                (for [[page result] results]
                  [:a {:href (common/href-viewer (:question_id path-params) page)
                       :target "_blank"}
                   [:div.text-gray-500 page ": " result]])]))
           [:hr.my-3.border]
           (new-soft-link req)])))
