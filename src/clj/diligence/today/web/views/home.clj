(ns diligence.today.web.views.home
    (:require
      [diligence.today.env :refer [host dev?]]
      [simpleui.core :as simpleui]
      [simpleui.response :as response]
      [diligence.today.web.controllers.project :as project]
      [diligence.today.web.htmx :refer [page-htmx
                                        defcomponent
                                        defcomponent-user]]
      [diligence.today.web.views.dropdown :as dropdown]
      [diligence.today.web.views.project-selector :as project-selector]))

(defn- logged-in? [req]
  (-> req :session :user_id boolean))

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
  (if user_id
   (project-selector/project-selector req)
   [:div
    [:img {:class "mt-20 mb-12 w-1/2 mx-auto"
           :src "/base_logo_transparent_background.png"}]
    [:div.flex.w-full.justify-center
     (if dev?
       [:a {:href "/api/gsi"} "Login"]
       logins)]]))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (if-let [{:keys [project_id]}
              (and (-> req :session :user_id)
                   (-> req (assoc :query-fn query-fn) project/get-project))]
       (response/redirect (format "/project/%s/" project_id))
       (page-htmx
        {:google? (not (logged-in? req))
         :hyperscript? (logged-in? req)}
        (-> req (assoc :query-fn query-fn) home))))))
