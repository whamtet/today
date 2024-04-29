(ns diligence.today.web.controllers.soft-link
    (:require
      [diligence.today.web.controllers.file :as file]))

(defn get-file-id [req question_id]
  (->> question_id (file/get-files req) first :file_id))

(defn get-soft-links [req question_id]
  (prn (get-file-id req question_id)))
