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
      [diligence.today.web.views.icons :as icons]
      [diligence.today.web.views.project-selector :as project-selector]))

(defn- logged-in? [req]
  (-> req :session :user_id boolean))

(defn- assoc-session [session & rest]
  (->> (apply assoc session rest)
       (assoc response/hx-refresh :session)))

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

[:div.w-16]
(defcomponent ^:endpoint account-selector [req command]
  (case command
    "view" (assoc-session session :view? true)
    "edit" (assoc-session session :view? true :edit? true)
    "admin" (assoc-session session :view? true :edit? true :admin? true)
        [:div {:class "mt-20 w-1/2 mx-auto border rounded-2xl cursor-pointer
        text-xl text-gray-700"}
         [:div {:class "flex items-center"
                :hx-post "account-selector:view"}
          [:div.mx-6 (icons/book-open-width 16)]
          "Read only account"]
         [:div {:class "flex items-center border-t"
                :hx-post "account-selector:edit"}
          [:div.mx-6 (icons/pencil-square-width 16)]
          "Editor account"]
         [:div {:class "flex items-center border-t"
                :hx-post "account-selector:admin"}
          [:div.mx-6 (icons/adjustments-horizontal-width 16)]
          "Admin account"]
         ]))

(defcomponent ^:endpoint home [req]
  (if user_id
    (if view?
      (project-selector/project-selector req)
      (account-selector req))
   [:div
    [:img {:class "mt-20 mb-12 w-1/2 mx-auto"
           :src "/base_logo_transparent_background.png"}]
    [:div.flex.w-full.justify-center
     (if dev?
       [:a#home {:href "/api/gsi"} "Login"]
       logins)]]))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (if-let [{:keys [project_id]}
              (and (-> req :session :view?)
                   (-> req (assoc :query-fn query-fn) project/get-random-project))]
       (response/redirect (format "/project/%s/" project_id))
       (page-htmx
        {:google? (not (logged-in? req))
         :hyperscript? (logged-in? req)}
        (-> req (assoc :query-fn query-fn) home))))))
