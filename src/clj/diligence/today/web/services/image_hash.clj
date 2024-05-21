(ns diligence.today.web.services.image-hash
    (:require
      [clojure.string :as string]
      [clojure.java.shell :refer [sh]]
      [diligence.today.web.services.file-locator :as file-locator])
    (:import
      java.io.File
      java.security.MessageDigest))

(defn md5 [s]
  (let [algorithm (MessageDigest/getInstance "MD5")
        raw (.digest algorithm (.getBytes s))]
    (format "%032x" (BigInteger. 1 raw))))

(defn- image-list [project_id dir index]
  (->> (file-locator/pdf-triple project_id dir index)
       (sh "pdfimages" "-list")
       :out))

(def image-line #"(\d+) +\d+ ([^\n]+)")

(defn hash! [project_id dir index]
  (->> (image-list project_id dir index)
       (re-seq image-line)
       (reduce
        (fn [m [_ k v]]
          (update m (Long/parseLong k) conj v))
        {})
       (map
        (fn [[page lines]]
          (->> lines
               string/join
               md5
               (spit (file-locator/image-hash project_id dir index page)))))
       dorun))

(defn slurp-hash [project_id dir index page]
  (let [f (File. (file-locator/image-hash project_id dir index page))]
    (when (.exists f)
          (slurp f))))
