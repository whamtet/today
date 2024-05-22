(ns diligence.today.web.views.common
    (:require
      [clojure.string :as string]
      [diligence.today.env :refer [dev?]]
      [diligence.today.util :refer [format-js]]
      [diligence.today.web.controllers.project :as project]
      [diligence.today.web.views.dropdown :as dropdown]))

(defn main-dropdown
  ([user_name] (main-dropdown user_name nil nil false))
  ([user_name project_id project-name admin?]
   [:div.absolute.top-1.right-1.flex.items-center
    (dropdown/dropdown
     (str "Welcome " user_name)
     (list
      (when project-name
            [:a {:href (format-js "/project/{project_id}/")} [:div.p-2 (str project-name "...")]])
      (when admin?
            [:a {:href (format-js "/project/{project_id}/admin/")} [:div.p-2 "Edit Questions..."]])
      (when admin?
            [:a {:href (format-js "/project/{project_id}/admin-file/")} [:div.p-2 "Edit Files..."]])
      [:div.p-2.cursor-pointer {:hx-post "/api/logout"} "Logout"]))]))

(def viewer-location
  (if dev?
    "http://localhost:8888/web/index.html"
    "https://app.simplifydd.com/pdf.js/web/"))

(defn- params [m]
  (->> m
       (remove #(-> % second nil?))
       (map (fn [[k v]] (format "%s=%s" (name k) v)))
       (string/join "&")))

(defn href-viewer [m]
  (format "%s?%s" viewer-location (params m)))
