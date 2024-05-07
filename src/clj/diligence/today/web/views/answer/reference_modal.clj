(ns diligence.today.web.views.answer.reference-modal
    (:require
      [clojure.string :as string]
      [clojure.walk :as walk]
      [diligence.today.web.controllers.iam :as iam]
      [diligence.today.web.controllers.question :as question]
      [diligence.today.web.htmx :refer [page-htmx defcomponent defcomponent-user]]
      [diligence.today.web.views.common :as common]
      [diligence.today.web.views.components :as components]
      [simpleui.core :as simpleui]
      [simpleui.render :as render]))

[:div {:class "w-2/3"}]
(defcomponent ^:endpoint reference-modal [req ^:long offset q]
  (components/modal-scroll "w-2/3"
                           [:div offset " " q " " (:question_id path-params)]))
