(ns diligence.today.web.controllers.fragment
    (:require
      [diligence.today.util :refer [mk]]))

(defn upsert-fragment [{:keys [query-fn]} fragment_id question_id fragment page]
  (query-fn :upsert-fragment (mk fragment_id question_id fragment page)))

(defn get-fragments [{:keys [query-fn]} question_id]
  (query-fn :get-fragments {:question_id question_id}))

(defn delete-fragment [{:keys [query-fn]} fragment_id]
  (query-fn :delete-fragment {:fragment_id fragment_id}))
