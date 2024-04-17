(ns diligence.today.web.controllers.user
    (:require
      [diligence.today.web.services.gsi :as gsi]))

(defn upsert-user [req]
  (when-let [info (gsi/req->user-info req)]
    (first
     ((:query-fn req) :upsert-user info))))
