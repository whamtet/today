(ns diligence.today.web.views.common
    (:require
      [diligence.today.web.views.dropdown :as dropdown]))

(defn main-dropdown [user_name]
  [:div.absolute.top-1.right-1.flex.items-center
   (dropdown/dropdown
    (str "Welcome " user_name)
    (->
     (list [:div.p-2.cursor-pointer {:hx-post "/api/logout"} "Logout"])
     (conj [:a {:href "/admin/"}
            [:div.p-2.cursor-pointer "Config..."]])))])
