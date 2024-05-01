(ns diligence.today.web.routes.ui
  (:require
   [diligence.today.web.middleware.exception :as exception]
   [diligence.today.web.middleware.formats :as formats]
   [diligence.today.web.views.home :as home]
   [diligence.today.web.views.pdf-viewer :as pdf-viewer]
   [diligence.today.web.views.question-editor :as question-editor]
   [diligence.today.web.views.question-maker :as question-maker]
   [diligence.today.web.views.question-viewer :as question-viewer]
   [integrant.core :as ig]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.parameters :as parameters]))

(defn route-data [opts]
  (merge
   opts
   {:muuntaja   formats/instance
    :middleware
    [;; Default middleware for ui
    ;; query-params & form-params
      parameters/parameters-middleware
      ;; encoding response body
      muuntaja/format-response-middleware
      ;; exception handling
      exception/wrap-exception]}))

(derive :reitit.routes/ui :reitit/routes)

(defmethod ig/init-key :reitit.routes/ui
  [_ opts]
  [["" (route-data opts) (home/ui-routes opts)]
   ["/project/:project_id" (route-data opts) (question-viewer/ui-routes opts)]
   ["/project/:project_id/admin" (route-data opts) (question-maker/ui-routes opts)]
   ["/project/:project_id/question/:question_id" (route-data opts) (question-editor/ui-routes opts)]
   ["/pdf-viewer" (route-data opts) (pdf-viewer/ui-routes opts)]])
