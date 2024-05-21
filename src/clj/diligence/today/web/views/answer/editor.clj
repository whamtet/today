(ns diligence.today.web.views.answer.editor
    (:require
      [diligence.today.env :refer [host]]
      [diligence.today.web.controllers.iam :as iam]
      [diligence.today.web.controllers.question :as question]
      [diligence.today.web.htmx :refer [page-htmx defcomponent defcomponent-user]]
      [diligence.today.util :as util :refer [format-js format-json mk-assoc]]
      [diligence.today.web.views.admin-file :as admin-file]
      [diligence.today.web.views.answer.reference-modal :as reference-modal]
      [diligence.today.web.views.common :as common]
      [diligence.today.web.views.components :as components]
      [simpleui.core :as simpleui]
      [simpleui.render :as render]))

[:sup {:class "reference text-blue-400 cursor-pointer relative text-red-500"}]
(defn- render-reference [path-params i {:keys [offset page file_id migration-pending?]}]
  [:sup {:class (str "reference cursor-pointer relative"
                     (if migration-pending?
                       " text-red-500"
                       " text-blue-400"))
         ;; can't use link because contenteditable = "true"
         :onclick (if migration-pending?
                    (->> (mk-assoc path-params file_id offset) admin-file/priority-migration (format-json "location.href = %s"))
                    (format-json "openPage(%s, %s)" file_id page))
         :data-offset offset} (inc i)
   [:span {:class "absolute w-80 -top-20 invisible"}
    [:img {:src (format-js "/api/thumbnail/{file_id}/{page}")}]]])

(defn- insert-references*
  [path-params
   i
   text
   [reference & rest]
   poffset
   done]
  (if-not reference
          (conj done text)
          (let [offset (- (:offset reference) poffset)]
            (if (>= offset (.length text))
              ;; drop remaining references
              (conj done text (render-reference path-params i reference) " ")
              (recur
                path-params
                (inc i)
                (.substring text offset)
                rest
                (:offset reference)
                (conj done (.substring text 0 offset) (render-reference path-params i reference)))))))

(defn- insert-references [path-params text references]
  (reverse
   (insert-references*
    path-params
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

(defn- render-editor [path-params {:keys [text references]}]
  (->> references
       vals
       (sort-by :offset)
       (insert-references path-params text)
       (map render-lines)))

(defn- get-editor [req question_id]
  (render-editor
   (:path-params req)
   (question/get-editor req question_id)))

(defn button [id label onclick]
  [:button {:id id
            :type "button"
            :onclick onclick
            :disabled true
            :class "bg-clj-blue py-1.5 px-3 rounded-lg text-white
            disabled:bg-gray-400"}
   label])

(defcomponent ^:endpoint editor [req text ^:json movements command]
  reference-modal/reference-modal
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
