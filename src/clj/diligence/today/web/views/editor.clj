(ns diligence.today.web.views.editor
    (:require
      [diligence.today.web.controllers.iam :as iam]
      [diligence.today.web.controllers.question :as question]
      [diligence.today.web.htmx :refer [page-htmx defcomponent defcomponent-user]]
      [diligence.today.web.views.common :as common :refer [href-viewer]]
      [diligence.today.web.views.components :as components]
      [simpleui.core :as simpleui]
      [simpleui.response :as response]))

(defn- get-editor [req question_id]
  (when-let [{:keys [text]} (question/get-editor req question_id)]))

(defn button [id label onclick]
  [:button {:id id
            :type "button"
            :onclick onclick
            :disabled true
            :class "bg-clj-blue py-1.5 px-3 rounded-lg text-white"}
   label])

(defcomponent ^:endpoint editor [req]
  (let [{:keys [text]} (get-editor req (:question_id path-params))]
    [:div
     (button "add-reference" "Add reference..." "addReference()")
     [:div#editor.mt-2.p-2.border {:contenteditable "true"}
      (or text "Write your answer here...")]]))
