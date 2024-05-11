(ns diligence.today.web.services.grep
    (:require
      [clojure.java.shell :refer [sh]]))

(defn- parse-line [s]
  (let [[_ page-num content] (re-find #"./(\d+).txt:(.+)" s)]
    [(Long/parseLong page-num) (.trim content)]))

(defn grep [project_id filter filename]
  (assert false "unimplemented")
  (when (-> filter count (>= 3))
        (some-> (sh "grep" "-ir" filter "." :dir (format "files/%s/grep/%s" project_id (.replaceAll filename ".pdf$" "")))
                :out
                .trim
                not-empty
                (.split "\n")
                (->> (take 10) (map parse-line)))))
