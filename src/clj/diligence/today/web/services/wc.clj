(ns diligence.today.web.services.wc
    (:require
      [clojure.java.shell :refer [sh]]
      [diligence.today.util :refer [format-js]]))

(defn- wc-src [project_id filename]
  (format "files/%s/grep/%s/*"
          project_id
          (.replaceAll filename ".pdf$" "")))
(defn- wc-file [project_id filename]
  (format "files/%s/grep/%s.wc"
          project_id
          (.replaceAll filename ".pdf$" "")))

(defn wc! [project_id filename]
  (sh "bash"
      "-c"
      (format-js "wc -l {(wc-src project_id filename)} > {(wc-file project_id filename)}")))

(defn wc [project_id filename]
  (->> (wc-file project_id filename)
       slurp
       (re-seq #"(^|\n)\s+(\d+)")
       butlast
       (map #(-> % last Long/parseLong))))

(prn (wc 1 "a.00.pdf"))
