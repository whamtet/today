(ns diligence.today.web.views.components
    (:require
      [diligence.today.web.views.icons :as icons]))

(defn warning [msg]
  [:span {:class "bg-red-600 p-2 rounded-lg text-white"} msg])

(defn hiddens [m]
  (for [[k v] m]
    [:input {:type "hidden" :name k :value v}]))

(defmacro hiddensm [& syms]
  (-> (map str syms)
      (zipmap syms)
      hiddens
      (conj 'list)))

(defn submit [value]
  [:input {:type "submit"
           :class "bg-clj-blue py-1.5 px-3 rounded-lg text-white"
           :value value}])

(defn button [label]
  [:button {:type "button"
            :class "bg-clj-blue py-1.5 px-3 rounded-lg text-white"}
   label])

(defn button-label [for label]
  [:label {:type "button"
           :for for
           :class "bg-clj-blue py-1.5 px-3 rounded-lg text-white cursor-pointer"}
   label])

(defn modal [width & contents]
  [:div#modal {:class "fixed left-0 top-0 w-full h-full
  z-10"
               :style {:background-color "rgba(0,0,0,0.4)"}
               :_ "on click if target.id === 'modal' add .hidden"}
   [:div {:class (str "mx-auto border rounded-lg bg-white " width)
          :style {:max-height "94vh"
                  :margin-top "3vh"
                  :margin-bottom "3vh"}}
    contents]])

(defn modal-scroll [width & contents]
  [:div#modal {:class "fixed left-0 top-0 w-full h-full
  z-10"
               :style {:background-color "rgba(0,0,0,0.4)"}
               :_ "on click if target.id === 'modal' add .hidden"}
   [:div {:class (str "mx-auto border rounded-lg bg-white overflow-y-auto overflow-x-clip " width)
          :style {:max-height "94vh"
                  :margin-top "3vh"
                  :margin-bottom "3vh"}}
    contents]])

(defn qtip [msg]
  [:div.tooltip.relative.text-gray-600 icons/qmark
   [:span.tooltiptext.invisible.absolute.w-48.border.p-2.rounded-md.bg-white msg]])
