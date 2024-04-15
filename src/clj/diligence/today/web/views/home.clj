(ns diligence.today.web.views.home
    (:require
      [simpleui.core :as simpleui :refer [defcomponent]]
      [diligence.today.web.htmx :refer [page-htmx]]))

(defcomponent ^:endpoint home [req]
  [:div "hi"])

(defn ui-routes [base-path]
  (simpleui/make-routes
   base-path
   (fn [req]
     (page-htmx
      {}
      (home req)))))
