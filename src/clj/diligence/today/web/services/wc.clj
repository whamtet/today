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
      (format-js "wc -l {(wc-src project_id filename)} > '{(wc-file project_id filename)}'")))

(defn- wc [project_id filename]
  (->> (wc-file project_id filename)
       slurp
       (re-seq #"(^|\n)\s+(\d+)")
       butlast
       (map #(-> % last Long/parseLong))))

(defn convert
  ([project_id filename global-line]
   (loop [[curr-wc & todo] (wc project_id filename)
          global-line global-line
          page 0]
     (if (< global-line curr-wc)
       [page global-line]
       (recur todo (- global-line curr-wc) (inc page)))))
  ([project_id filename page line]
   (->> (wc project_id filename)
        (take page)
        (apply + line))))
