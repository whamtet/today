(ns diligence.today.util
  (:require
    [clojure.data.json :as json]
    [clojure.string :as string]
    [diligence.today.env :refer [dev?]]))

(defmacro defm [sym & rest]
  `(def ~sym (memoize (fn ~@rest))))

(defmacro defm-dev [sym & rest]
  `(def ~sym ((if dev? identity memoize) (fn ~@rest))))

(defmacro mk [& syms]
  (zipmap (map keyword syms) syms))
(defmacro mk-assoc [m & syms]
  `(merge ~m (mk ~@syms)))

(defn uniqueness-violation? [e]
  (-> e str (.contains "SQLITE_CONSTRAINT_UNIQUE")))

(defn key-by [f s]
  (zipmap (map f s) s))

(defn bind [a x b]
  (-> x (max a) (min b)))

(defmacro format-js [s]
  `(-> ~s
    ~@(for [[to-replace replacement] (re-seq #"\{([^\}]+)}" s)]
       `(string/replace-first ~to-replace ~(read-string replacement)))))

(defn format-json [fmt & args]
  (->> args
       (map json/write-str)
       (apply format fmt)))

(defn some-item [f s]
  (some #(when (f %) %) s))

(defn map-vals [f m]
  (->> m vals (map f) (zipmap (keys m))))

(defn group-by-map [f1 f2 s]
  (->> s (filter f1) (group-by f1) (map-vals f2)))

(defn map-last [f s]
  (map-indexed
   (fn [i x]
     (f (-> i inc (= (count s))) x))
   s))

(defn parse-search [s]
  (into {}
        (for [kv (.split s "&")]
          (let [[k v] (.split kv "=")]
            [(keyword k)
             (if (re-find #"^\d" v)
               (Long/parseLong v)
               v)]))))

(defmacro format-js [s]
  `(-> ~s
    ~@(for [[to-replace replacement] (re-seq #"\{([^\}]+)}" s)]
       `(string/replace-first ~to-replace ~(read-string replacement)))))
