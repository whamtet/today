(ns diligence.today.web.routes.ui
    (:require
      [diligence.today.env :refer [dev?]]
      [diligence.today.web.middleware.exception :as exception]
      [diligence.today.web.middleware.formats :as formats]
      [diligence.today.web.views.admin :as admin]
      [diligence.today.web.views.admin-file :as admin-file]
      [diligence.today.web.views.answer :as answer]
      [diligence.today.web.views.home :as home]
      [diligence.today.web.views.pdf-viewer :as pdf-viewer]
      [diligence.today.web.views.question-viewer :as question-viewer]
      [integrant.core :as ig]
      [reitit.ring.middleware.muuntaja :as muuntaja]
      [reitit.ring.middleware.parameters :as parameters]
      [simpleui.response :as response]))

(defn log-out-redirect [handler]
  (fn [req]
    (if (-> req :session :user_id)
      (handler req)
      {:status 302, :headers {"Location" "/"}, :body ""})))

(defn log-out-admin? [handler]
  (fn [req]
    (if (-> req :session :admin?)
      (handler req)
      {:status 302, :headers {"Location" "/"}, :body ""})))

(defn log-out-static [handler]
  (if dev?
    handler
    (fn [req]
      (if (-> req :session :user_id)
        (handler req)
        (response/hx-redirect "/")))))

(defn route-data [opts]
  (merge
   opts
   {:muuntaja   formats/instance
    :middleware
    [
     parameters/parameters-middleware
      ;; encoding response body
     muuntaja/format-response-middleware
      ;; exception handling
     exception/wrap-exception]}))

(defn route-data-extra [opts extra]
  (merge
   opts
   {:muuntaja   formats/instance
    :middleware
    [extra
     parameters/parameters-middleware
     ;; encoding response body
     muuntaja/format-response-middleware
     ;; exception handling
     exception/wrap-exception]}))

(defn route-data-redirect [opts]
  (route-data-extra opts log-out-redirect))
(defn route-data-admin [opts]
  (route-data-extra opts log-out-admin?))
(defn route-data-static [opts]
  (route-data-extra opts log-out-static))

(derive :reitit.routes/ui :reitit/routes)

(defmethod ig/init-key :reitit.routes/ui
  [_ opts]
  [["" (route-data opts) (home/ui-routes opts)]
   ["/project/:project_id" (route-data-redirect opts) (question-viewer/ui-routes opts)]
   ["/project/:project_id/admin" (route-data-admin opts) (admin/ui-routes opts)]
   ["/project/:project_id/admin-file" (route-data-admin opts) (admin-file/ui-routes opts)]
   ["/project/:project_id/question/:question_id" (route-data-redirect opts) (answer/ui-routes opts)]
   ["/pdf-viewer" (route-data-static opts) (pdf-viewer/ui-routes opts)]])
