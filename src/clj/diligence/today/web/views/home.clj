(ns diligence.today.web.views.home
    (:require
      [diligence.today.env :refer [host]]
      [simpleui.core :as simpleui]
      [diligence.today.web.htmx :refer [page-htmx defcomponent]]
      [diligence.today.web.views.dropdown :as dropdown]))

(defn- logged-in? [req]
  (-> req :session :user_id boolean))

(defn main-dropdown [user_name]
  [:div.absolute.top-1.right-1.flex.items-center
   (dropdown/dropdown
    (str "Welcome " user_name)
    [[:a {:href ""}
      [:div.p-2 {:hx-post "/api/logout"}
       "Logout"]]])])

(def logins
  [:div
   [:div
    {:id "g_id_onload",
     :data-client_id (System/getenv "GSI_CLIENT")
     :data-context "signin",
     :data-ux_mode "redirect",
     :data-login_uri "https://diligence.today/api/gsi"
     :data-auto_prompt "false"}]
   [:div
    {:class "g_id_signin",
     :data-type "standard",
     :data-shape "rectangular",
     :data-theme "outline",
     :data-text "signin_with",
     :data-size "large",
     :data-logo_alignment "left"}]])

(defcomponent panel [req]
  [:div {:_ "on click add .hidden to .drop"}
   ;; header row
   [:div
    [:a.inline-block {:href "/"}
     [:img.w-16.m-2 {:src "/icon.png"}]]
    (main-dropdown user_id)]])

(defcomponent ^:endpoint home [req]
  (cond
   user_id (panel req)
   :else
   [:div
    [:img {:class "mt-20 mb-12 w-1/2 mx-auto"
           :src "/base_logo_transparent_background.png"}]
    [:div.flex.w-full.justify-center logins]]))

(defn ui-routes []
  (simpleui/make-routes
   ""
   (fn [req]
     (page-htmx
      {:google? (not (logged-in? req))
       :hyperscript? (logged-in? req)}
      (home req)))))
