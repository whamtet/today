(ns diligence.today.web.views.common
    (:require
      [diligence.today.web.views.dropdown :as dropdown]))

(defn main-dropdown [user_name project-selector?]
  [:div.absolute.top-1.right-1.flex.items-center
   (dropdown/dropdown
    (str "Welcome " user_name)
    (cond->
     (list [:div.p-2.cursor-pointer {:hx-post "/api/logout"} "Logout"])
     (not project-selector?) (conj [:a {:href "/"}
                                    [:div.p-2.cursor-pointer "Select Project..."]])))])
