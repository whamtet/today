(ns diligence.today.web.views.soft-links
    (:require
      [diligence.today.web.controllers.iam :as iam]
      [diligence.today.web.controllers.question :as question]
      [diligence.today.web.controllers.soft-link :as soft-link]
      [diligence.today.web.htmx :refer [defcomponent]]
      [diligence.today.web.views.common :as common]
      [diligence.today.web.views.components :as components]
      [diligence.today.web.views.dropdown :as dropdown]
      [diligence.today.web.views.icons :as icons]
      [simpleui.core :as simpleui]
      [simpleui.response :as response]))

(defcomponent ^:endpoint soft-links [req fragment_id command]
  (case command
        [:div
         [:h3 "Soft Links"]
         (for [{:keys [fragment_id fragment page]} (soft-link/get-soft-links req (:question_id path-params))]
           [:div])]))
