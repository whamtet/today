(ns diligence.today.web.routes.api
  (:require
    [diligence.today.env :refer [dev?]]
    [diligence.today.web.controllers.file :as file]
    [diligence.today.web.controllers.health :as health]
    [diligence.today.web.controllers.iam :as iam]
    [diligence.today.web.controllers.question :as question]
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

(def ok
  {:status 200
   :headers {}
   :body ""})

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
   ["/logout"
    (fn [_]
      (assoc response/hx-refresh :session {}))]
   ["/session"
    (fn [req]
      {:status 200
       :headers {"content-type" "text/html"}
       :body (-> req (dissoc :reitit.core/match :reitit.core/router) pr-str)})]
   (when dev?
         ["/test-reference"
          {:post (fn [req]
                   (question/set-editor
                    (assoc req :query-fn query-fn)
                    1
                    {:text "Write your answer here...",
                     :references {5 {:offset 5, :fragment "Page 1", :page 0, :line 0, :file_id 1 :migration-pending? true},
                                  26 {:offset 26, :fragment "Page 2", :page 1, :line 0, :file_id 1}}})
                   ok)
           :get (fn [req]
                  (let [editor (-> req (assoc :query-fn query-fn) (question/get-editor 1))]
                    (prn 'editor editor)
                    {:status 200
                     :headers {"content-type" "application/json"}
                     :body editor}))}])
   (when dev?
         ["/test-reference2"
          (fn [req]
            (question/set-editor
             (assoc req :query-fn query-fn)
             1
             {:text "Write your answer here...", :references {5 {:offset 5, :fragment "Page 1", :page 0, :line 0, :file_id 1, :migration-pending? true},
                                                              26 {:offset 26, :fragment "Page 2", :page 1, :line 0, :file_id 1 :migration-pending? true}}})
            (response/redirect
              "http://localhost:8888/web/index.html?migrate=true&file_id=1&project_id=1&file=http://localhost:2998/api/file/1"))])
   ["/file/:file_id"
    (fn [req]
      (if (-> req :session :user_id iam/prod-authorized?)
        (let [[mime body] (-> req
                              (assoc :query-fn query-fn)
                              (file/get-file-stream
                               (-> req :path-params :file_id)))]
          {:status 200
           :headers {"content-type" mime}
           :body body})
        {:status 401
         :headers {}
         :body ""}))]
   ;; retrievals
   ["/thumbnail/:file_id/:page"
    (fn [req]
      (-> req :session :user_id assert)
      {:status 200
       :headers {"content-type" "image/jpg"}
       :body (-> req
                 (assoc :query-fn query-fn)
                 (file/get-thumbnail-stream
                  (-> req :path-params :file_id)
                  (-> req :path-params :page Long/parseLong)))})]
   ["/health"
    {:get health/healthcheck!}]])

(derive :reitit.routes/api :reitit/routes)

(defmethod ig/init-key :reitit.routes/api
  [_ {:keys [base-path]
      :or   {base-path ""}
      :as   opts}]
  (fn [] [base-path route-data (api-routes opts)]))
