(ns diligence.today.web.htmx
  (:require
   [diligence.today.env :refer [dev?]]
   [diligence.today.web.controllers.user :as user]
   [diligence.today.web.resource-cache :as resource-cache]
   [simpleui.core :as simpleui]
   [simpleui.render :as render]
   [ring.util.http-response :as http-response]
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
  (cond-> (map #(vector :script {:src (resource-cache/cache-suffix %)}) (conj js "/common.js"))
          hyperscript? (conj
                        [:script {:src (unminify "https://unpkg.com/hyperscript.org@0.9.12/dist/_hyperscript.min.js")}])
          google? (conj [:script {:src "https://accounts.google.com/gsi/client" :async true :defer true}])))

(defn output [prefix]
  [:link {:rel "stylesheet"
          :href (str prefix
                     (resource-cache/cache-suffix "/output.css"))}])
(defn page-htmx [options & body]
  (page
   [:head
    [:meta {:charset "UTF-8"}]
    [:title "DDIndex"]
    [:meta {:property "og:title" :content "DDIndex"}]
    [:meta {:property "og:type" :content "website"}]
    [:meta {:property "og:url" :content "https://ddindex.simpleui.io/"}]
    [:meta {:property "og:image" :content "https://ddindex.simpleui.io/icon.png"}]
    [:link {:rel "icon" :href "/icon.png"}]
    (output "")]
   [:body
    [:div#modal.hidden]
    (render/walk-attrs body)
    [:script {:src
              (unminify "https://unpkg.com/htmx.org@1.9.5/dist/htmx.min.js")}]
    [:script "htmx.config.defaultSwapStyle = 'outerHTML';"]
    (scripts options)]))

(defmacro defcomponent
  [name [req :as args] & body]
  (if-let [sym (simpleui/symbol-or-as req)]
    `(simpleui/defcomponent ~name ~args
      (let [{:keys [~'session ~'path-params]} ~sym
            {:keys [~'user_id ~'view? ~'edit? ~'admin?]} ~'session
            ~'project_id (some-> ~'path-params :project_id Long/parseLong)]
        ~@body))
    (throw (Exception. "req ill defined"))))

(defmacro defcomponent-user
  [name [req :as args] & body]
  (if-let [sym (simpleui/symbol-or-as req)]
    `(simpleui/defcomponent ~name ~args
      (let [{:keys [~'session ~'path-params]} ~sym
            {:keys [~'user_id ~'view? ~'edit? ~'admin?]} ~'session
            ~'project_id (some-> ~'path-params :project_id Long/parseLong)
            {:keys [~'first_name]} (user/get-user-exception ~sym)]
        ~@body))
    (throw (Exception. "req ill defined"))))
