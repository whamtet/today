(ns diligence.today.web.controllers.question
    (:require
      [clojure.java.io :as io]))

(def suggestions
  (-> "suggestions.txt"
       io/resource
       slurp
      .trim
      (.split "\n")
      (->>
       (keep
        (fn [line]
          (let [line (.trim line)]
            (when-not (or (empty? line) (.startsWith line "#"))
                      line)))))))

(defn get-questions [{:keys [query-fn]} project_id]
  (query-fn :get-questions {:project_id project_id}))

(defn add-question [{:keys [query-fn]} project_id question]
  (query-fn :insert-question {:project_id project_id
                              :question question}))

(defn update-question [{:keys [query-fn]} question_id question]
  (query-fn :update-question {:question_id question_id
                              :question question}))
