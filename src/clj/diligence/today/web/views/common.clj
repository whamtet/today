(ns diligence.today.web.views.common
    (:require
      [clojure.string :as string]
      [diligence.today.env :refer [dev?]]
      [diligence.today.web.views.dropdown :as dropdown]))

(defn main-dropdown
  ([user_name] (main-dropdown user_name nil))
  ([user_name project_id]
   [:div.absolute.top-1.right-1.flex.items-center
    (dropdown/dropdown
     (str "Welcome " user_name)
     (list
      (when project_id
            [:a {:href (format "/project/%s/admin/" project_id)} [:div.p-2 "Project Config..."]])
      [:div.p-2.cursor-pointer {:hx-post "/api/logout"} "Logout"]))]))

(def viewer-location
  (if dev?
    "http://localhost:8888/web/viewer.html"
    "https://app.simplifydd.com/pdf.js/web/viewer.html"))

(defn- params [m]
  (->> m
       (map (fn [[k v]] (format "%s=%s" (name k) v)))
       (string/join "&")))

(defn href-viewer [m]
  (format "%s?%s" viewer-location (params m)))
