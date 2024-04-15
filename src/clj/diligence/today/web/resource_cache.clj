(ns diligence.today.web.resource-cache
    (:require
      [clojure.java.io :as io]
      [diligence.today.env :refer [dev?]]))

(def hash-resource
  ((if dev? identity memoize)
   (fn [src]
     (->> src
          (str "public")
          io/resource
          slurp
          hash))))

(defn cache-suffix [src]
  (->> src
       hash-resource
       (str src "?hash=")))
