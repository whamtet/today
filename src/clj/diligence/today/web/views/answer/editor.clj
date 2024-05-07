(ns diligence.today.web.views.answer.editor
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

(def default-text "Write your answer here...")
(defn- render-reference [i {:keys [offset page]}]
  [:sup {:class "reference text-blue-400 cursor-pointer"
         :onclick (format "openPage(%s)" page)
         :data-offset offset} (inc i)])

(defn- insert-references*
  [i
   text
   [reference & rest]
   poffset
   done]
  (if-not reference
          (conj done text)
          (let [offset (- (:offset reference) poffset)]
            (if (>= offset (.length text))
              ;; drop remaining references
              (conj done text (render-reference i reference) " ")
              (recur
                (inc i)
                (.substring text offset)
                rest
                (:offset reference)
                (conj done (.substring text 0 offset) (render-reference i reference)))))))

(defn- insert-references [text references]
  (reverse
   (insert-references*
    0
    text
    references
    0
    ())))

(defn- render-lines [s]
  (if (string? s)
    (-> s
        (.replace "\n" "<br>")
        (.replace " " "&nbsp;"))
    s))

(defn- render-editor [{:keys [text references]}]
  (->> references
       vals
       (sort-by :offset)
       (insert-references text)
       (map render-lines)))

(defn- get-editor [req question_id]
  (if-let [state (question/get-editor req question_id)]
    (render-editor state)
    default-text))

(defn button [id label onclick]
  [:button {:id id
            :type "button"
            :onclick onclick
            :disabled true
            :class "bg-clj-blue py-1.5 px-3 rounded-lg text-white
            disabled:bg-gray-400"}
   label])

(defcomponent ^:endpoint editor [req text ^:json movements command]
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
