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

[:sup.text-red-500.text-xl]
(defn insert-sup [text offset index]
  (let [offset (min offset (count text))]
    (str
     (.substring text 0 offset)
     "<sup class=\"text-red-500 text-xl\">" (inc index) "</sup>"
     (when (< offset (count text))
           (.substring text offset)))))

(defn- parse-current-url [req]
  (-> (get-in req [:headers "hx-current-url"])
      (.split "\\?")
      last
      util/parse-search))

(defmacro with-src-params [req & body]
  `(let [~'src-params (parse-current-url ~req)]
    ~@body))

(defcomponent ^:endpoint inset [req
                                ^:long question_id
                                ^:json values
                                ^:long disp-index]
  (when question_id
        (question/assoc-reference req question_id values))
  (with-src-params req
                   (let [{:keys [project_id file_id]} src-params
                         to-migrate (question/get-pending-file req project_id file_id)
                         max-index (dec (count to-migrate))
                         disp-index (or disp-index 0)
                         {:keys [text question offset index
                                 question_id]}
                         (some-> to-migrate not-empty (nth (util/bind 0 disp-index max-index)))]
                     (if (empty? to-migrate)
                       (->> project_id (format "/project/%s/admin-file/") host response/hx-redirect)
                       (list
                        [:script
                         (format "migration_offset = %s;" offset)]
                        (if-init
                         [:div {:hx-target "this"}
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
                          ]))))))

(defcomponent ^:endpoint pdf-viewer [req
                                     ^:long question_id
                                     ^:json values]
  (with-src-params req
                   (if question_id
                     (let [[{:keys [project_id]}] (question/assoc-reference
                                                   req
                                                   question_id
                                                   values)]
                       (response/hx-redirect
                        (host (format-js "/project/{project_id}/question/{question_id}/"))))
                     (if (:migrate src-params)
                       (inset req)
                       [:form {:class "hidden"
                               :id "values-form"
                               :hx-post "pdf-viewer"
                               :hx-vals {:question_id (:question_id src-params)}}
                        [:input#values {:type "hidden" :name "values"}]]))))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes-simple
    (host "/pdf-viewer/")
    [query-fn]
    pdf-viewer))
