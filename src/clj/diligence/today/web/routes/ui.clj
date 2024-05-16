(ns diligence.today.web.routes.ui
  (:require
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
   [reitit.ring.middleware.parameters :as parameters]))

(defn log-out-redirect [handler]
  (fn [req]
    (if (-> req :session :user_id)
      (handler req)
      {:status 302, :headers {"Location" "/"}, :body ""})))

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

(defn route-data-redirect [opts]
  (merge
   opts
   {:muuntaja   formats/instance
    :middleware
    [log-out-redirect
     parameters/parameters-middleware
     ;; encoding response body
     muuntaja/format-response-middleware
     ;; exception handling
     exception/wrap-exception]}))

(derive :reitit.routes/ui :reitit/routes)

(defmethod ig/init-key :reitit.routes/ui
  [_ opts]
  [["" (route-data opts) (home/ui-routes opts)]
   ["/project/:project_id" (route-data-redirect opts) (question-viewer/ui-routes opts)]
   ["/project/:project_id/admin" (route-data-redirect opts) (admin/ui-routes opts)]
   ["/project/:project_id/admin-file" (route-data-redirect opts) (admin-file/ui-routes opts)]
   ["/project/:project_id/question/:question_id" (route-data-redirect opts) (answer/ui-routes opts)]
   ["/pdf-viewer" (route-data opts) (pdf-viewer/ui-routes opts)]])
