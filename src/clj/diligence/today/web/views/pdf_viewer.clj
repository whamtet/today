(ns diligence.today.web.views.pdf-viewer
    (:require
      [diligence.today.env :refer [host]]
      [diligence.today.util :as util]
      [diligence.today.web.controllers.iam :as iam]
      [diligence.today.web.controllers.fragment :as fragment]
      [diligence.today.web.controllers.question :as question]
      [diligence.today.web.htmx :refer [defcomponent]]
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

(defn- parse-search [s]
  (into {}
        (for [kv (.split s "&")]
          (let [[k v] (.split kv "=")]
            [(keyword k)
             (if (re-find #"^\d" v)
               (Long/parseLong v)
               v)]))))

(def drag-scripts
  (list
   [:script {:src "https://unpkg.com/interactjs/dist/interact.min.js"}]
   [:script {:src "matt/drag.js"}]))

(defmacro if-init [body]
  `(if (simpleui/get? ~'req)
    (list
     (inset-disp ~body)
      drag-scripts)
    ~body))

(defcomponent ^:endpoint inset [{:keys [headers] :as req}
                                ^:long disp-index]
  (let [{:keys [project_id file_id]} (-> "hx-current-url" headers (.split "\\?") last parse-search)
        to-migrate (question/get-pending-file req project_id file_id)
        current-question (->> (util/bind 0 (or disp-index 0) (count to-migrate))
                              (nth to-migrate))]
    (list
     [:script (->> to-migrate count (= 1) (format "is_final = %s"))]
     (if-init
      [:div {:hx-target "this"}
       (pr-str current-question)]))))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes-simple
    (host "/pdf-viewer/")
    [query-fn]
    inset))
