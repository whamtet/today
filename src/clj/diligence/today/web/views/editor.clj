(ns diligence.today.web.views.editor
    (:require
      [diligence.today.env :refer [dev?]]
      [diligence.today.web.controllers.file :as file]
      [diligence.today.web.controllers.fragment :as fragment]
      [diligence.today.web.controllers.iam :as iam]
      [diligence.today.web.controllers.question :as question]
      [diligence.today.web.htmx :refer [page-htmx defcomponent defcomponent-user]]
      [diligence.today.web.views.common :as common :refer [href-viewer]]
      [diligence.today.web.views.components :as components]
      [diligence.today.web.views.dropdown :as dropdown]
      [diligence.today.web.views.icons :as icons]
      [diligence.today.web.views.soft-links :as soft-links]
      [simpleui.core :as simpleui]
      [simpleui.response :as response]))

(def answer "Well, that's an interesting question.  We don't have a complete answer.

But I can suggest a few

More tricks?


for example")

(def editor
  {:answer answer
   :footnotes
   [{:start (.indexOf answer "interesting")
     :end (.indexOf answer ".")}]})

(defcomponent ^:endpoint editor [req]
  [:div#editor.m-10 {:contenteditable "true"}
   (.replace answer "\n" "<br>")])
