(ns diligence.today.web.controllers.question
    (:require
      [clojure.java.io :as io]
      [diligence.today.util :as util :refer [mk]]))

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

(defn get-question-text [{:keys [query-fn]} project_id question]
  (query-fn :get-question-text
            {:project_id project_id
             :question question}))

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

(defn set-editor [{:keys [query-fn]} question_id editor]
  (query-fn :update-editor {:question_id question_id
                            :editor (pr-str editor)}))

(defn- update-editor [req question_id f & args]
  (set-editor
   req
   question_id
   (apply f (get-editor req question_id) args)))

(defn- move-references [references movements]
  (->> references
       vals
       (keep
        #(when-let [new-offset (-> % :offset movements)]
          (assoc % :offset new-offset)))
       (util/key-by :offset)))

(defn update-editor-text [req question_id text movements]
  (update-editor
   req
   question_id
   #(-> %
     (assoc :text (.replace text " " " "))
     (update :references move-references movements))))

(defn assoc-reference [req question_id reference]
  (update-editor req question_id assoc-in [:references (:offset reference)] reference))

(defn assoc-reference-page [req
                            question_id
                            offset
                            file_id
                            page]
  (assoc-reference req
                   question_id
                   (mk offset file_id page)))
