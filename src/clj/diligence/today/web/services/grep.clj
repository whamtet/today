(ns diligence.today.web.services.grep
    (:require
      [clojure.java.shell :refer [sh]]))

(defn- parse-line [s]
  (let [[_ page-num content] (re-find #"./(\d+).txt:(.+)" s)]
    [(Long/parseLong page-num) (.trim content)]))

(defn grep [filter filename]
  (-> (sh "grep" "-ir" filter "." :dir (str "files/grep/" (.replace filename ".pdf" "")))
      :out
      .trim
      (.split "\n")
      (->> (map parse-line))))
