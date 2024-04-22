(ns diligence.today.web.views.home
    (:require
      [diligence.today.env :refer [host]]
      [simpleui.core :as simpleui]
      [simpleui.response :as response]
      [diligence.today.web.controllers.iam :as iam]
      [diligence.today.web.controllers.project :as project]
      [diligence.today.web.htmx :refer [page-htmx
                                        defcomponent
                                        defcomponent-user]]
      [diligence.today.web.views.components :as components]
      [diligence.today.web.views.dropdown :as dropdown]))

(defn- logged-in? [req]
  (-> req :session :user_id boolean))

(defn assoc-session [{:keys [session]} k v]
  (assoc response/hx-refresh :session (assoc session k v)))

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

(defn- uniqueness-violation? [e]
  (-> e str (.contains "SQLITE_CONSTRAINT_UNIQUE")))

(defcomponent-user ^:endpoint project-selector [req command project-name]
  (case command
        "new" (iam/when-authorized
               (try
                 (project/create-project req project-name)
                 (assoc-session req :project-name project-name)
                 (catch clojure.lang.ExceptionInfo e
                   (if (uniqueness-violation? e)
                     [:div#unique-warning.my-3 (components/warning "Project name in use.")]
                     (throw e)))))
        [:div {:_ "on click add .hidden to .drop"}
         ;; header row
         [:div
          [:a.inline-block {:href "/"}
           [:img.w-16.m-2 {:src "/icon.png"}]]
          (main-dropdown first_name)]
         [:div {:class "w-1/2 border rounded-lg mx-auto"}
          [:form {:class "flex"
                  :hx-post "project-selector:new"
                  :hx-target "#unique-warning"}
           [:input {:class "p-2 w-full"
                    :name "project-name"
                    :placeholder "Create project..."
                    :required true}]
           [:input {:class "bg-clj-blue p-1.5 rounded-lg text-white w-24"
                    :type "submit"
                    :value "Create"}]]
          [:div#unique-warning]]]))

(defcomponent ^:endpoint home [req]
  (cond
   user_id (project-selector req)
   :else
   [:div
    [:img {:class "mt-20 mb-12 w-1/2 mx-auto"
           :src "/base_logo_transparent_background.png"}]
    [:div.flex.w-full.justify-center logins]]))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:google? (not (logged-in? req))
       :hyperscript? (logged-in? req)}
      (-> req (assoc :query-fn query-fn) home)))))
