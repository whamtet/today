(ns diligence.today.web.views.question-maker
    (:require
      [diligence.today.web.controllers.iam :as iam]
      [diligence.today.web.controllers.project :as project]
      [diligence.today.web.htmx :refer [defcomponent-user]]
      [diligence.today.web.views.common :as common]
      [diligence.today.web.views.components :as components]
      [diligence.today.web.views.dropdown :as dropdown]
      [simpleui.response :as response]))

(defcomponent-user ^:endpoint question-maker [req]
  [:div {:_ "on click add .hidden to .drop"}
   ;; header row
   (common/header-row first_name true)
   [:div {:class "w-3/4 border rounded-lg mx-auto"}]])
