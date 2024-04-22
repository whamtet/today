(ns diligence.today.web.views.common
    (:require
      [diligence.today.web.views.dropdown :as dropdown]))

(defn main-dropdown [user_name project-selector?]
  [:div.absolute.top-1.right-1.flex.items-center
   (dropdown/dropdown
    (str "Welcome " user_name)
    (cond->
     (list [:div.p-2.cursor-pointer {:hx-post "/api/logout"} "Logout"])
     (not project-selector?) (conj [:div.p-2.cursor-pointer
                                    {:hx-post "project-selector:unselect"} "Select Project..."])))])

(defn header-row [user_name project-selector?]
  [:div
   [:a.inline-block {:href "/"}
    [:img.w-16.m-2 {:src "/icon.png"}]]
   (main-dropdown user_name project-selector?)])
