(ns diligence.today.web.views.editor
    (:require
      [clojure.string :as string]
      [diligence.today.web.controllers.iam :as iam]
      [diligence.today.web.controllers.question :as question]
      [diligence.today.web.htmx :refer [page-htmx defcomponent defcomponent-user]]
      [diligence.today.web.views.common :as common :refer [href-viewer]]
      [diligence.today.web.views.components :as components]
      [simpleui.core :as simpleui]
      [simpleui.render :as render]))

(def default-text "Write your answer here...")
(defn- render-reference [i {:keys [offset page]}]
  (render/html ""
               [:sup {:class "reference text-blue-400 cursor-pointer"
                      :onclick (format "openPage(%s)" page)
                      :data-offset offset} (inc i)]))

(defn- insert-references* [text references]
  (loop [i 0
         text text
         [reference & rest] references
         poffset 0
         done ()]
    (if-not reference
            (conj done text)
            (let [offset (- (:offset reference) poffset)]
              (if (>= offset (.length text))
                ;; drop remaining references
                (conj done text (render-reference i reference))
                (recur
                  (inc i)
                  (.substring text offset)
                  rest
                  (:offset reference)
                  (conj done (.substring text 0 offset) (render-reference i reference))))))))

(defn- insert-references [text references]
  (-> (insert-references* text references) reverse string/join))

(defn- render-line [line]
  [:div
   (if (-> line .trim empty?)
     "&nbsp"
     line)])
(defn- render-lines [s]
  (map render-line (.split s "\n")))

(defn- render-editor [{:keys [text references]}]
  (->> references
       vals
       (sort-by :offset)
       (insert-references text)
       render-lines))

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
