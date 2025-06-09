(ns diligence.today.web.services.grep
    (:require
      [diligence.today.web.services.file-locator :as file-locator]
      [diligence.today.web.services.image-hash :as image-hash]
      [diligence.today.util :as util :refer [format-js]]
      [clojure.java.io :as io]))

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

(defn fragment-line
  "line number of a fragment of text"
  [project_id dir index page fragment]
  (let [s (slurp (file-locator/grep-file project_id dir index page))]
    (when (-> (.split s fragment) count (= 2))
          (->> (.split s "\n")
               (take-while #(not (.contains % fragment)))
               count))))

(defn- slurp-page [project_id dir index page]
  [(slurp (file-locator/grep-file project_id dir index page))
   (image-hash/slurp-hash project_id dir index page)])
(defn- slurp-range [project_id dir index pages]
  (->> pages
       range
       (map #(slurp-page project_id dir index %))))

(defn- split-at3 [n s]
  (let [[a [b & c]] (split-at n s)]
    [b (concat a c)]))

(defn whole-page
  "returns index of page in new document when it uniquely matches the old one"
  [project_id dir index pages_old pages page]
  (assert (pos? index))
  (let [[page others] (->> (slurp-range project_id dir (dec index) pages_old)
                           (split-at3 page))]
    (when (every? #(not= page %) others)
          (let [[[page2] & rest] (->> (slurp-range project_id dir index pages)
                                      (map-indexed list)
                                      (filter #(-> % second (= page))))]
            (when-not rest page2)))))
