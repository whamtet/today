(ns diligence.today.web.services.diff
    (:require
      [clojure.java.shell :refer [sh]]))

(defn diff-file [project_id filename]
  (format "files/%s/grep/%s.txt"
          project_id
          (.replaceAll filename ".pdf$" "")))

(defn- split-lines [done s]
  (let [i (.indexOf s "\n")]
    (if (<= 0 i)
      (-> done
          (conj (.substring s 0 i))
          (recur (.substring s (inc i))))
      ;; ignore final \n
      done)))

(defn diff-output [project_id f1 f2]
  (let [{:keys [exit out]} (sh "diff" "-y" "-W" "1" (diff-file project_id f2) (diff-file project_id f1))]
    (assert (< exit 2))
    (when (= 1 exit)
          (->> out split-lines (map #(.trim %))))))

(defn new-line [project_id f1 f2 line]
  (if-let [diff-output (diff-output project_id f1 f2)]
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
