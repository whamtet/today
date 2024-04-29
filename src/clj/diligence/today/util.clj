(ns diligence.today.util
  (:require
    [diligence.today.env :refer [dev?]]))

(defmacro defm [sym & rest]
  `(def ~sym (memoize (fn ~@rest))))

(defmacro defm-dev [sym & rest]
  `(def ~sym ((if dev? identity memoize) (fn ~@rest))))

(defmacro mk [& syms]
  (zipmap (map keyword syms) syms))
