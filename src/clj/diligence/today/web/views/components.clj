(ns diligence.today.web.views.components)

(defn warning [msg]
  [:span {:class "bg-red-600 p-2 rounded-lg text-white"} msg])
