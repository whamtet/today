(ns diligence.today.web.services.diff
    (:require
      [clojure.java.shell :refer [sh]]
      [diligence.today.web.services.file-locator :refer [diff-file]]))

(defn diff-output [project_id dir old-index]
  (let [{:keys [exit out]} (sh "diff" "-y" "-W" "1"
                               (diff-file project_id dir (inc old-index))
                               (diff-file project_id dir old-index))]
    (assert (< exit 2))
    (when (= 1 exit)
          (->> out
               (re-seq #" ?([<\|>]?) ?\n")
               (map second)))))

(defn new-line [project_id dir old-index line]
  (if-let [diff-output (diff-output project_id dir old-index)]
    (do
      (assert (< line (count diff-output)))
      (loop [[curr-line & todo] diff-output
             i 0
             line line]
        (if (= i line)
          (when (empty? curr-line) line)
          (case curr-line
                "" (recur todo (inc i) line)
                "|" (recur todo (inc i) line)
                ">" (recur todo i (dec line))
                "<" (recur todo (inc i) (inc line))))))
    ;; files are identical - line unchanged
    line))
