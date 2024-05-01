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

(defn get-question [{:keys [query-fn]} question_id]
  (query-fn :get-question {:question_id question_id}))

(defn get-questions [{:keys [query-fn]} project_id]
  (query-fn :get-questions {:project_id project_id}))

(defn delete-question [{:keys [query-fn]} question_id]
  (query-fn :delete-question {:question_id question_id}))

(defn add-question [{:keys [query-fn]} project_id question]
  (query-fn :insert-question {:project_id project_id
                              :question question}))

(defn update-question [{:keys [query-fn]} question_id question]
  (query-fn :update-question {:question_id question_id
                              :question question}))

(defn get-suggestions [req project_id]
  (remove
   (->> (get-questions req project_id) (map :question) set)
   suggestions))

(defn get-editor [req question_id]
  (some-> (get-question req question_id)
          :editor
          read-string))

(defn update-editor [{:keys [query-fn]} question_id editor]
  (query-fn :update-editor {:question_id question_id
                            :editor (pr-str editor)}))
