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

(defn fragment-row [{:keys [fragment_id fragment page]}]
  [:div.flex.mb-2
   [:span {:class "w-1/6 text-gray-500 text-center cursor-pointer"
           :onclick (str "PDFViewerApplication.page = " page)}
    (format "(pp %s)" page)]
   [:div {:class "w-2/3 truncate"} fragment]
   [:div {:class "w-1/6 text-gray-500 cursor-pointer"
          :hx-delete "inset:del"
          :hx-vals {:fragmentId fragment_id}}
    [:div.mx-auto.w-6 icons/trash]]])

(defn- inset-disp [question-title fragments]
  [:div#inset {:class "p-2 w-96 overflow-y-auto overflow-x-clip
  border"
               :hx-target "this"
               :style "position: fixed;
top: 50px;
right: 100px;
height: 300px;
background-color: white;"}
   [:h3.text-center.mb-3 question-title]
   [:div
    (map fragment-row fragments)]])

(defn- parse-search [s]
  (into {}
        (for [kv (.split s "&")]
          (let [[k v] (.split kv "=")]
            [(keyword k)
             (if (re-find #"^\d" v)
               (Long/parseLong v)
               v)]))))

(defcomponent ^:endpoint inset [{:keys [headers] :as req}
                               fragment
                               fragmentId
                               page
                               command]
  (let [{:keys [question_id]} (-> "hx-current-url" headers (.split "\\?") last parse-search)
        _ (case command
                "add" (fragment/upsert-fragment
                       req fragmentId question_id fragment page)
                "del" (fragment/delete-fragment req fragmentId)
                nil)
        {:keys [question]} (question/get-question req question_id)]
    [:div
     (inset-disp question (fragment/get-fragments req question_id))
     [:div#modal]]))

(prn 'host (host "/pdf-viewer/"))
(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes-simple
    (host "/pdf-viewer/")
    [query-fn]
    inset))
