(ns diligence.today.web.controllers.fragment
    (:require
      [diligence.today.util :refer [mk]]
      [diligence.today.web.controllers.soft-link :as soft-link]))

(defmacro with-file-id [& body]
  `(let [~'file_id (soft-link/get-file-id ~'req ~'question_id)]
    ~@body))

(defn upsert-fragment [{:keys [query-fn] :as req} fragment_id question_id fragment page]
  (with-file-id
   (query-fn :upsert-fragment (mk fragment_id file_id fragment page))))

(defn get-fragments [{:keys [query-fn] :as req} question_id]
  (with-file-id
   (sort-by :page
            (query-fn :get-fragments {:file_id file_id}))))

(defn delete-fragment [{:keys [query-fn]} fragment_id]
  (query-fn :delete-fragment {:fragment_id fragment_id}))
