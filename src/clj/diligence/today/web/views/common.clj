(ns diligence.today.web.views.common
    (:require
      [diligence.today.web.views.dropdown :as dropdown]))

(defn main-dropdown [user_name]
  [:div.absolute.top-1.right-1.flex.items-center
   (dropdown/dropdown
    (str "Welcome " user_name)
    (list
     [:a {:href "/"} [:div.p-2 "Manage Project..."]]
     [:a {:href "/admin/"} [:div.p-2 "Config..."]]
     [:div.p-2.cursor-pointer {:hx-post "/api/logout"} "Logout"]))])
