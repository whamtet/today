(ns diligence.today.web.views.soft-links
    (:require
      [diligence.today.web.controllers.iam :as iam]
      [diligence.today.web.controllers.question :as question]
      [diligence.today.web.controllers.soft-link :as soft-link]
      [diligence.today.web.htmx :refer [defcomponent]]
      [diligence.today.web.services.grep :as grep]
      [diligence.today.web.views.common :as common]
      [diligence.today.web.views.components :as components]
      [diligence.today.web.views.dropdown :as dropdown]
      [diligence.today.web.views.icons :as icons]
      [simpleui.core :as simpleui]
      [simpleui.response :as response]))

(defcomponent ^:endpoint soft-links [req fragment_id command]
  (let [{:keys [file_id filename]} (soft-link/get-file req (:question_id path-params))]
    (case command
          [:div
           [:h3 "Soft Links"]
           (for [q (soft-link/get-soft-links req file_id)]
             (let [results (grep/grep q filename)]
               [:div [:h4 q]
                (for [result results]
                  [:div.text-gray-500 result])]))])))
