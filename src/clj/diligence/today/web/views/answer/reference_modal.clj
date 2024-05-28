(ns diligence.today.web.views.answer.reference-modal
    (:require
      [diligence.today.env :refer [host]]
      [diligence.today.util :as util :refer [format-js]]
      [diligence.today.web.controllers.file :as file]
      [diligence.today.web.controllers.question :as question]
      [diligence.today.web.htmx :refer [defcomponent]]
      [diligence.today.web.views.common :as common]
      [diligence.today.web.views.components :as components]
      [simpleui.response :as response]))

(defcomponent ^:endpoint page-img [req ^:long file_id ^:long page ^:long offset ^:nullable q]
  (assert edit?)
  (let [file (file/get-file req file_id)
        page (-> page (max 1) (min (:pages file)))]
    [:a#page-img {:href (common/href-viewer
                         {:file_id file_id
                          :page (dec page)
                          :offset offset
                          :question_id (:question_id path-params)
                          :file (host (format-js "/api/file/{file_id}"))
                          :q q})}
     [:img {:class "w-80"
            :src (format-js "/api/thumbnail/{file_id}/{(dec page)}")}]]))

[:div {:class "w-3/4"}]
(defmacro modal [body]
  `(if ~'selected_file ;; must be an update
    ~body
    (components/modal-scroll "w-3/4" ~body)))

(defn- thumbnail-src [{:keys [file_id filename_original]}]
  (if (.endsWith filename_original ".pdf")
    (format-js "/api/thumbnail/{file_id}/0")
    "/excel_icon.svg"))

[:div {:class "w-40 cursor-pointer border-2 border-clj-blue mr-2"}]
(defcomponent ^:endpoint reference-modal [req
                                          ^:long offset
                                          ^:nullable q
                                          ^:long selected_file
                                          ;; for insertion only
                                          ^:long page
                                          command]
  (assert edit?)
  (case command
        "page-direct" (do
                       (question/assoc-reference-page req (:question_id path-params) offset selected_file (dec page))
                       response/hx-refresh)
        "file-direct" (do
                        (question/assoc-reference-file req (:question_id path-params) offset selected_file)
                        response/hx-refresh)
        (modal
         (let [files (file/get-files req project_id)
               file (or
                     (util/some-item #(-> % :file_id (= selected_file)) files)
                     (first files))]
           [:div.p-2 {:hx-target "this"}
            ;; select row
            [:div.flex.items-start
             (map
              (fn [{:keys [file_id filename_original] :as this-file}]
                [:div
                 [:div.text-center filename_original]
                 [:img {:class (cond-> "w-40 cursor-pointer mr-2"
                                       (-> file :file_id (= file_id))
                                       (str " border-2 border-clj-blue"))
                        :src (thumbnail-src this-file)
                        :hx-get "reference-modal"
                        :hx-vals {:offset offset
                                  :q q
                                  :selected_file file_id}}]]) files)]
            (if (-> file :filename_original (.endsWith ".pdf"))
              (list
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
               (page-img req (:file_id file) 1 offset q))
              ;; can only reference entire document
              [:div {:class "my-2"
                      :hx-post "reference-modal:file-direct"
                      :hx-vals {:selected_file (:file_id file)
                                :offset offset}
                      :hx-confirm "Reference entire document?"}
               (components/button "Reference file")]
              )]))))
