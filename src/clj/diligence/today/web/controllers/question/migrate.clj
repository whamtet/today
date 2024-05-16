(ns diligence.today.web.controllers.question.migrate
    (:require
      [diligence.today.util :as util]
      [diligence.today.web.controllers.file :as file]
      [diligence.today.web.controllers.question :as question]))

;; fun times!

(defn- migrate-editor [req]
  (fn [{:keys [file_id page line] :as m}]
    (if-let [new-page (file/whole-page req file_id page)]
      (assoc m :page new-page)
      (if-let [[new-page new-line] (file/move-line req file_id page line)]
        (assoc m :page new-page :line new-line) ;; might need to put migration-pending? here too
        (assoc m :migration-pending? true)))))

(defn mark-migrated [req project_id file_id]
  (doseq [{:keys [question_id editor]} (question/get-questions-file req project_id file_id)]
    (->> editor
         (util/map-vals (migrate-editor req))
         (question/set-editor req project_id))))
