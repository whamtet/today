(ns diligence.today.web.services.image-hash
    (:require
      [clojure.string :as string]
      [clojure.java.shell :refer [sh]])
    (:import
      java.io.File
      java.security.MessageDigest))

(defn md5 [s]
  (let [algorithm (MessageDigest/getInstance "MD5")
        raw (.digest algorithm (.getBytes s))]
    (format "%032x" (BigInteger. 1 raw))))

(defn- hash-file [project_id filename page]
  (format "files/%s/grep/%s/%03d.ihash"
          project_id
          (.replaceAll filename ".pdf$" "")
          page))

(defn- image-list [project_id filename]
  (:out
   (sh "pdfimages" "-list"
       (format "files/%s/%s" project_id filename))))

(def image-line #"(\d+) +\d+ ([^\n]+)")

(defn hash! [project_id filename]
  (->> (image-list project_id filename)
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
               (spit (hash-file project_id filename page)))))
       dorun))

(defn slurp-hash [project_id filename page]
  (let [f (File. (hash-file project_id filename page))]
    (when (.exists f)
          (slurp f))))
