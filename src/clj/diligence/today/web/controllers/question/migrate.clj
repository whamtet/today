(ns diligence.today.web.controllers.question.migrate
    (:require
      [diligence.today.util :as util]
      [diligence.today.web.controllers.file :as file]
      [diligence.today.web.controllers.question :as question]))

(defn- migrate-editor-value [req file_id m]
  (if (-> m :file_id (= file_id))
    (if-let [new-page (file/whole-page req file_id (:page m))]
      (assoc m :page new-page)
      (if-let [[new-page new-line] (file/move-line req file_id (:page m) (:line m))]
        (assoc m :page new-page :line new-line) ;; might need to put migration-pending? here too
        (assoc m :migration-pending? true)))
    m))

(defn migrate-questions [req project_id file_id]
  (let [migrated (map
                  (fn [question]
                    (update question
                            :editor
                            (fn [editor]
                              (util/map-vals #(migrate-editor-value req file_id %) editor))))
                  (question/get-questions-file req project_id file_id))]
    (doseq [{:keys [question_id editor]} migrated]
      (question/set-editor req question_id editor))
    (some
     #(->> % :editor vals (some :migration-pending?))
     migrated)))


(defn mark-migrated [req project_id file_id]
  (doseq [{:keys [question_id editor]} (question/get-questions-file req project_id file_id)]
    (->> editor
         (util/map-vals (migrate-editor req))
         (question/set-editor req question_id))))
