(ns diligence.today.web.middleware.core
  (:require
    [diligence.today.env :as env]
    [ring.middleware.defaults :as defaults]
    [ring.middleware.session.cookie :as cookie]))

(def cors-headers
  {"Access-Control-Allow-Origin" "*"
   "Access-Control-Allow-Methods" "POST, GET, OPTIONS, DELETE"
   "Access-Control-Allow-Headers" "*"
   "Access-Control-Expose-Headers" "*"})

(defn wrap-cors [handler]
  (fn [req]
    (if (-> req :request-method (= :options))
      {:status 200
       :headers cors-headers
       :body ""}
      (-> req
          handler
          (update :headers merge cors-headers)))))

(defn wrap-base
  [{:keys [metrics site-defaults-config cookie-secret] :as opts}]
  (let [cookie-store (cookie/cookie-store {:key (.getBytes ^String cookie-secret)})]
    (fn [handler]
      (cond-> ((:middleware env/defaults) handler opts)
              env/dev? wrap-cors
              true (defaults/wrap-defaults
                     (assoc-in site-defaults-config [:session :store] cookie-store))
              ))))
