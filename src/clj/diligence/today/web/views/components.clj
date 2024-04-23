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
