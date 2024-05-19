(ns diligence.today.web.views.pdf-viewer
    (:require
      [diligence.today.env :refer [host]]
      [diligence.today.util :as util]
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

(defn- inset-disp [& body]
  [:div {:class "draggable p-2 overflow-y-auto overflow-x-clip
  border bg-white fixed"
               :style "top: 50px;
right: 100px;
width: 700px;
height: 300px;"}
   [:div {:hx-target "this"} body]])

(def drag-scripts
  (list
   [:script {:src "https://unpkg.com/interactjs/dist/interact.min.js"}]
   [:script {:src "matt/drag.js"}]))

(defmacro if-init [body]
  `(if (simpleui/get? ~'req)
    (list
     (output (host))
     (inset-disp ~body)
      drag-scripts)
    ~body))

(defn left-arrow [disp-index]
  [:div {:class "cursor-pointer"
         :hx-post "inset"
         :hx-vals {:disp-index (dec disp-index)}}
   icons/left-arrow])
(defn right-arrow [disp-index]
  [:div {:class "curosr-pointer"
         :hx-post "inset"
         :hx-vals {:disp-index (inc disp-index)}}
   icons/right-arrow])

(defcomponent ^:endpoint inset [{:keys [headers] :as req}
                                ^:long disp-index]
  (let [{:keys [project_id file_id]} (-> "hx-current-url"
                                         headers
                                         (.split "\\?")
                                         last
                                         util/parse-search)
        to-migrate (question/get-pending-file req project_id file_id)
        max-index (dec (count to-migrate))
        disp-index (or disp-index 0)
        {:keys [text question offset]} (->> (util/bind 0 disp-index max-index)
                                     (nth to-migrate))]
    (list
     [:script (->> to-migrate count (= 1) (format "is_final = %s"))]
     (if-init
      [:div {:hx-target "this"}
       [:h3.text-gray-500 question]
       (for [paragraph (.split text "\n")]
         [:p.mt-2 paragraph])
       [:div.flex.absolute.left-2.bottom-2
        (when (pos? disp-index) (left-arrow (dec disp-index)))
        (when (< disp-index max-index) (right-arrow (inc disp-index)))]
       ]))))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes-simple
    (host "/pdf-viewer/")
    [query-fn]
    inset))
