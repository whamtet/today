(ns diligence.today.web.views.answer.reference-modal
    (:require
      [clojure.string :as string]
      [clojure.walk :as walk]
      [diligence.today.web.controllers.iam :as iam]
      [diligence.today.web.controllers.question :as question]
      [diligence.today.web.htmx :refer [page-htmx defcomponent defcomponent-user]]
      [diligence.today.web.views.common :as common :refer [href-viewer]]
      [diligence.today.web.views.components :as components]
      [simpleui.core :as simpleui]
      [simpleui.render :as render]))

(defcomponent ^:endpoint reference-modal [req text ^:json movements command]
  (case command
        "text"
        (iam/when-authorized
         (question/update-editor-text req (:question_id path-params) text movements)
         nil)
        [:div
         [:script (format "question_id = %s" (:question_id path-params))]
         (button "add-reference" "Add reference..." "addReference()")
         [:div#editor.mt-2.p-2.border {:contenteditable "true"
                                       :onblur "saveEditor()"}
          (get-editor req (:question_id path-params))]]))
