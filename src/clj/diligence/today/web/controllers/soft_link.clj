(ns diligence.today.web.controllers.soft-link
    (:require
      [diligence.today.web.controllers.file :as file]))

(defn get-file [req question_id]
  (->> question_id (file/get-files req) first))

(defmacro with-file-id [& body]
  `(let [{:keys [~'file_id]} (get-file ~'req ~'question_id)]
    ~@body))

(defn get-soft-links [{:keys [query-fn] :as req} file_id]
  (->>
   (query-fn :get-soft-links {:file_id file_id})
   (map :q)
   sort))

(defn insert-soft-link [req question_id q]
  (with-file-id
   ((:query-fn req) :insert-soft-link {:file_id file_id
                                       :q q})))

(defn delete-soft-link [req question_id q]
  (with-file-id
   ((:query-fn req) :delete-soft-link {:q q :file_id file_id})))
