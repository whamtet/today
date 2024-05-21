(ns diligence.today.web.views.pdf-viewer
    (:require
      [diligence.today.env :refer [host]]
      [diligence.today.util :as util :refer [format-js]]
      [diligence.today.web.controllers.iam :as iam]
      [diligence.today.web.controllers.fragment :as fragment]
      [diligence.today.web.controllers.question :as question]
      [diligence.today.web.htmx :refer [output defcomponent]]
      [diligence.today.web.views.common :as common]
      [diligence.today.web.views.components :as components]
      [diligence.today.web.views.dropdown :as dropdown]
      [diligence.today.web.views.icons :as icons]
      [simpleui.core :as simpleui]
      [simpleui.response :as response]))

(defn- inset-disp [body]
  [:div {:class "draggable p-2 overflow-y-auto overflow-x-clip
  border bg-white fixed"
               :style "top: 50px;
right: 100px;
width: 700px;
height: 300px;"}
   body])

(def drag-scripts
  (list
   [:script {:src "https://unpkg.com/interactjs/dist/interact.min.js"}]
   [:script {:src "matt/drag.js"}]))

(defmacro if-init [body]
  `(if (simpleui/get? ~'req)
    (list*
     (output (host))
     (inset-disp ~body)
      drag-scripts)
    (list ~body)))

(defn left-arrow [disp-index]
  [:div {:class "cursor-pointer"
         :hx-post "inset"
         :hx-vals {:disp-index (dec disp-index)}}
   icons/left-arrow])
(defn right-arrow [disp-index]
  [:div {:class "cursor-pointer"
         :hx-post "inset"
         :hx-vals {:disp-index (inc disp-index)}}
   icons/right-arrow])

[:sup.text-red-500.text-xl]
(defn insert-sup [text offset index]
  (let [offset (min offset (count text))]
    (str
     (.substring text 0 offset)
     "<sup class=\"text-red-500 text-xl\">" (inc index) "</sup>"
     (when (< offset (count text))
           (.substring text offset)))))

(defcomponent ^:endpoint inset [req
                                ^:long question_id
                                ^:json values
                                ^:long disp-index]
  (when question_id
        (question/assoc-reference req question_id values))
  (let [{:keys [project_id file_id]} (:src-params req)
        to-migrate (question/get-pending-file req project_id file_id)
        max-index (dec (count to-migrate))
        disp-index (or disp-index 0)
        {:keys [text question offset index
                question_id]}
        (some-> to-migrate not-empty (nth (util/bind 0 disp-index max-index)))]
    (if (empty? to-migrate)
      (->> project_id (format "/project/%s/admin-file/") host response/hx-redirect)
      (list*
       [:script
        (format "migration_offset = %s;" offset)]
       (if-init
        [:div {:class "cursor-default"
               :hx-target "this"}
         [:form {:class "hidden"
                 :id "values-form"
                 :hx-post "inset"}
          [:input {:type "hidden" :name "question_id" :value question_id}]
          [:input#values {:type "hidden" :name "values"}]]
         [:h3.text-gray-500 question]
         (for [paragraph (.split (insert-sup text offset index) "\n")]
           [:p.mt-2 paragraph])
         [:div.flex.absolute.left-2.bottom-2
          (when (pos? disp-index) (left-arrow (dec disp-index)))
          (when (< disp-index max-index) (right-arrow (inc disp-index)))]
         ])))))

(defcomponent ^:endpoint pdf-viewer [req
                                     ^:long question_id
                                     ^:json values]
  (if question_id
    (let [[{:keys [project_id]}] (question/assoc-reference
                                  req
                                  question_id
                                  values)]
      (response/hx-redirect
       (host (format-js "/project/{project_id}/question/{question_id}/"))))
    (if (-> req :src-params :migrate)
      (inset req)
      [:form {:class "hidden"
              :id "values-form"
              :hx-post "pdf-viewer"
              :hx-vals {:question_id (-> req :src-params :question_id)}}
       [:input#values {:type "hidden" :name "values"}]])))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes-simple
    (host "/pdf-viewer/")
    [query-fn]
    pdf-viewer))
