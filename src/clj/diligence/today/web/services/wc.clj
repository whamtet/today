(ns diligence.today.web.services.wc
    (:require
      [clojure.java.shell :refer [sh]]
      [diligence.today.web.services.file-locator :refer [wc-src wc-file]]
      [diligence.today.util :refer [format-js]]))

(defn wc! [project_id dir index]
  (sh "bash"
      "-c"
      (format-js "wc -l {(wc-src project_id dir index)} > {(wc-file project_id dir index)}")))

(defn- wc [project_id dir index]
  (->> (wc-file project_id dir index)
       slurp
       (re-seq #"(^|\n)\s+(\d+)")
       butlast
       (map #(-> % last Long/parseLong))))

(defn convert
  "convert between global-line and [page line]"
  ([project_id dir index global-line]
   (loop [[curr-wc & todo] (wc project_id dir index)
          global-line global-line
          page 0]
     (if (< global-line curr-wc)
       [page global-line]
       (recur todo (- global-line curr-wc) (inc page)))))
  ([project_id dir index page line]
   (->> (wc project_id dir index)
        (take page)
        (apply + line))))
