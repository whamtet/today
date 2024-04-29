(ns diligence.today.web.views.pdf-viewer
    (:require
      [diligence.today.web.controllers.iam :as iam]
      [diligence.today.web.controllers.project :as project]
      [diligence.today.web.controllers.question :as question]
      [diligence.today.web.htmx :refer [defcomponent]]
      [diligence.today.web.views.common :as common]
      [diligence.today.web.views.components :as components]
      [diligence.today.web.views.dropdown :as dropdown]
      [diligence.today.web.views.icons :as icons]
      [simpleui.core :as simpleui]
      [simpleui.response :as response]))

(defn- inset [question-title]
  [:div {:class "p-2"
         :style "position: fixed;
top: 50px;
right: 100px;
width: 400px;
height: 300px;
background-color: white;
border: 1px solid lightgray;"}
   [:h3.text-center question-title]])

(defcomponent ^:endpoint init [{:keys [headers] :as req}]
  (let [question_id (-> "hx-current-url" headers (.split "=") last Long/parseLong)
        {:keys [question]} (question/get-question req question_id)]
    [:div
     (inset question)
     [:div#modal]]))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes-simple
    "http://localhost:3000/pdf-viewer/"
    [query-fn]
    init))
