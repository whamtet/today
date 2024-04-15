(ns diligence.today.web.htmx
  (:require
   [diligence.today.env :refer [dev?]]
   [diligence.today.web.resource-cache :as resource-cache]
   [simpleui.render :as render]
   [ring.util.http-response :as http-response]
   [hiccup.core :as h]
   [hiccup.page :as p]))

(defn page [opts & content]
  (-> (p/html5 opts content)
      http-response/ok
      (http-response/content-type "text/html")))

(defn- unminify [^String s]
  (if dev?
    (.replace s ".min" "")
    s))

(defn- scripts [js hyperscript?]
  (cond-> js
          hyperscript? (conj (unminify "https://unpkg.com/hyperscript.org@0.9.12/dist/_hyperscript.min.js"))))

(defn page-htmx [{:keys [js hyperscript?]} & body]
  (page
   [:head
    [:meta {:charset "UTF-8"}]
    [:title "diligence.today"]
    ;[:link {:rel "icon" :href "/logo_dark.svg"}]
    [:link {:rel "stylesheet" :href (resource-cache/cache-suffix "/output.css")}]]
   [:body
    (render/walk-attrs body)
    [:script {:src
              (unminify "https://unpkg.com/htmx.org@1.9.5/dist/htmx.min.js")}]
    [:script "htmx.config.defaultSwapStyle = 'outerHTML';"]
    (map
     (fn [src]
       [:script {:src src}])
     (scripts js hyperscript?))]))
