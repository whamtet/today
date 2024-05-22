(ns diligence.today.web.controllers.user
    (:require
      [diligence.today.env :refer [dev?]]
      [diligence.today.web.services.gsi :as gsi]))

(def dev-info
  {:email "whamtet@gmail.com"
   :given_name "Matthew"
   :family_name "Molloy"
   :picture "https://lh3.googleusercontent.com/a/ACg8ocLhx96bXmR5tudqOH7skC0KdKxLgmw1FiROxCJXG6AA56SQDU6L=s96-c"})

(defn upsert-user [req]
  (when-let [info (if dev? dev-info (gsi/req->user-info req))]
    ;; returns {:user_id 1}
    (first
     ((:query-fn req) :upsert-user info))))

(defn get-user-exception [{:keys [query-fn session]}]
  (or (query-fn :get-user-by-id session)
      (throw (ex-info "logged-out" {:type :logged-out}))))
