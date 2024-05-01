(ns diligence.today.web.views.common
    (:require
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

(defn href-viewer
  ([project_id]
   (format "%s?project_id=%s" viewer-location project_id))
  ([project_id page]
   (format "%s?project_id=%s&page=%s" viewer-location project_id page)))
