(ns diligence.today.web.services.grep
    (:require
      [diligence.today.util :as util :refer [format-js]]
      [clojure.java.io :as io]))

(defn- grep-file [project_id filename page]
  (format "files/%s/grep/%s/%03d.txt"
          project_id
          (.replaceAll filename ".pdf$" "")
          page))

#_#_
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

(defn fragment-line [project_id filename page fragment]
  (let [s (slurp (grep-file project_id filename page))]
    (when (-> (.split s fragment) count (= 2))
          (->> (.split s "\n")
               (take-while #(not (.contains % fragment)))
               count))))
