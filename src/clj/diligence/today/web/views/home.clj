(ns diligence.today.web.views.home
    (:require
      [diligence.today.env :refer [host]]
      [simpleui.core :as simpleui :refer [defcomponent]]
      [diligence.today.web.htmx :refer [page-htmx]]))

(defn- logged-in? [req]
  (-> req :session :id boolean))

(def logins
  [:div
   [:div
    {:id "g_id_onload",
     :data-client_id (System/getenv "GSI_CLIENT")
     :data-context "signin",
     :data-ux_mode "redirect",
     :data-login_uri (host "/api/gsi")
     :data-auto_prompt "false"}]
   [:div
    {:class "g_id_signin",
     :data-type "standard",
     :data-shape "rectangular",
     :data-theme "outline",
     :data-text "signin_with",
     :data-size "large",
     :data-logo_alignment "left"}]])

(defcomponent ^:endpoint home [req]
  [:div
   [:h1.text-center.mt-32 "diligence.today"]
   [:div.mt-8.flex.w-full.justify-center logins]
   ])

(defn ui-routes [base-path]
  (simpleui/make-routes
   base-path
   (fn [req]
     (page-htmx
      {:google? (not (logged-in? req))}
      (home req)))))
