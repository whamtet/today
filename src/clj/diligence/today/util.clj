(ns diligence.today.util
  (:require
    [clojure.string :as string]
    [diligence.today.env :refer [dev?]]))

(defmacro defm [sym & rest]
  `(def ~sym (memoize (fn ~@rest))))

(defmacro defm-dev [sym & rest]
  `(def ~sym ((if dev? identity memoize) (fn ~@rest))))

(defmacro mk [& syms]
  (zipmap (map keyword syms) syms))

(defn uniqueness-violation? [e]
  (-> e str (.contains "SQLITE_CONSTRAINT_UNIQUE")))

(defn key-by [f s]
  (zipmap (map f s) s))

(defmacro format-js [s]
  `(-> ~s
    ~@(for [[to-replace replacement] (re-seq #"\{([^\}]+)}" s)]
       `(string/replace-first ~to-replace ~(read-string replacement)))))

(defn some-item [f s]
  (some #(when (f %) %) s))
