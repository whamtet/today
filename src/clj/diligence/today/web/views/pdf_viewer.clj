(ns diligence.today.web.views.pdf-viewer
    (:require
      [diligence.today.env :refer [host]]
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
  [:div#inset {:class "p-2 overflow-y-auto overflow-x-clip
  border bg-white fixed"
               :hx-target "this"
               :style "top: 50px;
right: 100px;
width: 500px;
height: 300px;"} body])

(defn- parse-search [s]
  (into {}
        (for [kv (.split s "&")]
          (let [[k v] (.split kv "=")]
            [(keyword k)
             (if (re-find #"^\d" v)
               (Long/parseLong v)
               v)]))))

(defcomponent ^:endpoint inset [{:keys [headers] :as req}
                                ^:long disp-index]
  (let [{:keys [project_id file_id]} (-> "hx-current-url" headers (.split "\\?") last parse-search)
        ]
    [:div
     (inset-disp (pr-str (question/get-pending-file req project_id file_id)))
     [:div#modal.hidden]]))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes-simple
    (host "/pdf-viewer/")
    [query-fn]
    inset))
