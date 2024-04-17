(ns diligence.today.web.routes.api
  (:require
    [diligence.today.web.controllers.health :as health]
    [diligence.today.web.controllers.user :as user]
    [diligence.today.web.middleware.exception :as exception]
    [diligence.today.web.middleware.formats :as formats]
    [integrant.core :as ig]
    [reitit.coercion.malli :as malli]
    [reitit.ring.coercion :as coercion]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.parameters :as parameters]
    [reitit.swagger :as swagger]
    [simpleui.response :as response]))

(def route-data
  {:coercion   malli/coercion
   :muuntaja   formats/instance
   :swagger    {:id ::api}
   :middleware [;; query-params & form-params
                parameters/parameters-middleware
                  ;; content-negotiation
                muuntaja/format-negotiate-middleware
                  ;; encoding response body
                muuntaja/format-response-middleware
                  ;; exception handling
                coercion/coerce-exceptions-middleware
                  ;; decoding request body
                muuntaja/format-request-middleware
                  ;; coercing response bodys
                coercion/coerce-response-middleware
                  ;; coercing request parameters
                coercion/coerce-request-middleware
                  ;; exception handling
                exception/wrap-exception]})

;; Routes
(defn api-routes [{:keys [query-fn]}]
  [["/swagger.json"
    {:get {:no-doc  true
           :swagger {:info {:title "diligence.today API"}}
           :handler (swagger/create-swagger-handler)}}]
   ["/gsi"
    (fn [req]
      (-> req
          (assoc :query-fn query-fn)
          user/upsert-user
          (->> (assoc (response/redirect "/") :session))))]
   #_
   ["/session"
    (fn [req]
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (-> req (dissoc :reitit.core/match :reitit.core/router) pr-str)})]
   ["/health"
    {:get health/healthcheck!}]])

(derive :reitit.routes/api :reitit/routes)

(defmethod ig/init-key :reitit.routes/api
  [_ {:keys [base-path]
      :or   {base-path ""}
      :as   opts}]
  (fn [] [base-path route-data (api-routes opts)]))
