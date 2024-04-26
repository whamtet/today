(ns diligence.today.web.views.components)

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

(defn button [label]
  [:button {:type "button"
            :class "bg-clj-blue py-1.5 px-3 rounded-lg text-white"}
   label])
