(ns diligence.today.web.htmx
  (:require
   [diligence.today.env :refer [dev?]]
   [diligence.today.web.resource-cache :as resource-cache]
   [simpleui.core :as simpleui]
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

(defn- scripts [{:keys [js hyperscript? google?]}]
  (cond-> (map #(vector :script {:src %}) js)
          hyperscript? (conj
                        [:script {:src (unminify "https://unpkg.com/hyperscript.org@0.9.12/dist/_hyperscript.min.js")}])
          google? (conj [:script {:src "https://accounts.google.com/gsi/client" :async true :defer true}])))

(defn page-htmx [options & body]
  (page
   [:head
    [:meta {:charset "UTF-8"}]
    [:title "diligence.today"]
    [:link {:rel "icon" :href "/logo.ico"}]
    [:link {:rel "stylesheet" :href (resource-cache/cache-suffix "/output.css")}]]
   [:body
    (render/walk-attrs body)
    [:script {:src
              (unminify "https://unpkg.com/htmx.org@1.9.5/dist/htmx.min.js")}]
    [:script "htmx.config.defaultSwapStyle = 'outerHTML';"]
    (scripts options)]))

(defmacro defcomponent
  [name [req :as args] & body]
  (if-let [sym (simpleui/symbol-or-as req)]
    `(simpleui/defcomponent ~name ~args
      (let [{:keys [~'session]} ~sym
            {:keys [~'user_id]} ~'session]
        ~@body))
    (throw (Exception. "req ill defined"))))
