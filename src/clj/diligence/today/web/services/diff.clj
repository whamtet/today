(ns diligence.today.web.services.diff
    (:require
      [clojure.java.shell :refer [sh]]))

(defn diff-file [project_id filename]
  (format "files/%s/grep/%s.txt"
          project_id
          (.replaceAll filename ".pdf$" "")))

(defn diff-output [project_id f1 f2]
  (-> (sh "diff" "-y" "-W" "1" (diff-file project_id f2) (diff-file project_id f1))
      :out
      (.split "\n")
      (->> (map #(.trim %)))))

(defn new-line [project_id f1 f2 line]
  (loop [[curr-line & todo] (diff-output project_id f1 f2)
         i 0
         line line]
    (if (= i line)
      (when (empty? curr-line) line)
      (case curr-line
            "" (recur todo (inc i) line)
            "|" (recur todo (inc i) line)
            ">" (recur todo i (dec line))
            "<" (recur todo (inc i) (inc line))))))
