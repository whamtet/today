(ns diligence.today.web.controllers.question.migrate
    (:require
      [diligence.today.util :as util]
      [diligence.today.web.controllers.file :as file]
      [diligence.today.web.controllers.question :as question]))

(defn- migrate-editor-value [req file_id m]
  (if (and (-> m :file_id (= file_id))
           (-> m :migration-pending? not))
    (if-let [new-page (file/whole-page req file_id (:page m))]
      (assoc m :page new-page)
      (if-let [[new-page new-line] (some->> m
                                            :line
                                            (file/move-line req file_id (:page m)))]
        (assoc m :page new-page :line new-line) ;; might need to put migration-pending? here too
        (assoc m :migration-pending? true)))
    m))

(defn- migrate-questions
  "migrate all editor references to file_id"
  [req project_id file_id]
  (let [migrated (map
                  (fn [question]
                    (update-in question
                               [:editor :references]
                               (fn [references]
                                 (util/map-vals #(migrate-editor-value req file_id %) references))))
                  (question/get-questions-file req project_id file_id))]
    (doseq [{:keys [question_id editor]} migrated]
      (question/set-editor req question_id editor))
    (some
     #(->> % :editor :references vals (some :migration-pending?))
     migrated)))

(defn migrate-file [req project_id file file_id]
  (if file_id
    (do
      (file/copy-file req project_id file file_id)
      (migrate-questions req project_id file_id))
    (do
      (file/new-file req project_id file)
      nil)))

(defn delete-file [req project_id file_id]
  (file/delete-file req project_id file_id)
  (question/nullify-file-references req project_id file_id))
