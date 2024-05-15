(ns diligence.today.web.views.answer.reference-modal
    (:require
      [diligence.today.util :as util :refer [format-js]]
      [diligence.today.web.controllers.iam :as iam]
      [diligence.today.web.controllers.file :as file]
      [diligence.today.web.controllers.question :as question]
      [diligence.today.web.htmx :refer [defcomponent]]
      [diligence.today.web.views.common :as common]
      [diligence.today.web.views.components :as components]
      [simpleui.response :as response]))

(defcomponent ^:endpoint page-img [req ^:long file_id ^:long page ^:long offset q]
  (let [file (file/get-file req file_id)
        page (-> page (max 1) (min (:pages file)))]
    [:a#page-img {:href (common/href-viewer
                         {:file_id file_id
                          :page page
                          :offset offset
                          :question_id (:question_id path-params)
                          :q q})
                  :target "_blank"}
     [:img {:class "w-80"
            :src (format-js "/api/thumbnail/{file_id}/{(dec page)}")}]]))

[:div {:class "w-3/4"}]
(defmacro modal [body]
  `(if ~'selected_file ;; must be an update
    ~body
    (components/modal-scroll "w-3/4" ~body)))

[:div {:class "w-80 cursor-pointer border-2 border-clj-blue"}]
(defcomponent ^:endpoint reference-modal [req
                                          ^:long offset
                                          ^:nullable q
                                          ^:long selected_file
                                          ;; for insertion only
                                          ^:long page
                                          command]
  (case command
        "page-direct" (iam/when-authorized
                       (question/assoc-reference-page req (:question_id path-params) offset selected_file (dec page))
                       response/hx-refresh)
        (modal
         (let [files (file/get-files req project_id)
               file (or
                     (util/some-item #(-> % :file_id (= selected_file)) files)
                     (first files))]
           [:div.p-2 {:hx-target "this"}
            ;; select row
            [:div.flex.items-center
             (map
              (fn [{:keys [file_id]}]
                [:img {:class (cond-> "w-40 cursor-pointer"
                                      (-> file :file_id (= file_id))
                                      (str " border-2 border-clj-blue"))
                       :src (format-js "/api/thumbnail/{file_id}/0")
                       :hx-get "reference-modal"
                       :hx-vals {:offset offset
                                 :q q
                                 :selected_file file_id}}]) files)]

            [:div.flex.items-center.my-2
             [:span.mr-1 "Page"]
             [:input {:class "p-1 border rounded-md mr-1 w-14"
                      :id "page-input"
                      :type "number"
                      :name "page"
                      :value 1
                      :min 1
                      :max (:pages file)
                      :hx-get "page-img"
                      :hx-vals {:file_id (:file_id file)
                                :offset offset
                                :q q}
                      :hx-target "#page-img"}]
             [:span {:hx-post "reference-modal:page-direct"
                     :hx-vals {:selected_file (:file_id file)
                               :offset offset}
                     :hx-include "#page-input"
                     :hx-confirm "Reference entire page?"}
              (components/button "Reference whole page")]]
            (page-img req (:file_id file) 1 offset q)]))))
