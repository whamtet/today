(ns diligence.today.web.controllers.question
    (:require
      [clojure.java.io :as io]
      [diligence.today.util :as util :refer [mk mk-assoc]]
      [diligence.today.web.controllers.file :as file]))

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

(def suggestions-section
  (-> "suggestions.txt"
      io/resource
      slurp
      .trim
      (.split "\n")
      (->>
       (keep
        (fn [line]
          (let [line (.trim line)]
            (when (.startsWith line "#")
                  (.substring line 2))))))))

(defn get-question [{:keys [query-fn]} question_id]
  (query-fn :get-question {:question_id question_id}))

(defn get-questions-flat [{:keys [query-fn]} project_id]
  (query-fn :get-questions-flat {:project_id project_id}))
(defn get-questions [{:keys [query-fn]} project_id]
  (->> {:project_id project_id}
       (query-fn :get-questions)
       (util/group-by-map #(select-keys % [:ordering :section :section_id])
                          #(filter :question %))
       (sort-by #(-> % first :ordering))))
(defn get-sections [{:keys [query-fn]} project_id]
  (query-fn :get-sections {:project_id project_id}))

(defn get-question-text [{:keys [query-fn]} project_id question]
  (query-fn :get-question-text
            {:project_id project_id
             :question question}))

(defn delete-question [{:keys [query-fn]} question_id]
  (query-fn :delete-question {:question_id question_id}))
(defn delete-section [{:keys [query-fn]} section_id]
  (query-fn :delete-section {:section_id section_id}))

(def base-editor
  {:text "Write your answer here..." :references {}})
(defn add-question [{:keys [query-fn]} project_id section_id question]
  (query-fn :insert-question {:section_id section_id
                              :project_id project_id
                              :question question
                              :editor (pr-str base-editor)}))

(defn update-question [{:keys [query-fn]} question_id question]
  (query-fn :update-question {:question_id question_id
                              :question question}))

(defn get-suggestions [req project_id]
  (remove
   (->> (get-questions-flat req project_id) (map :question) set)
   suggestions))

(defn get-suggestions-section [req project_id]
  (remove
   (->> (get-sections req project_id) (map :section) set)
   suggestions-section))

(defn insert-section [{:keys [query-fn]} project_id section]
  (query-fn :insert-section {:project_id project_id
                             :section section}))

(defn update-section [{:keys [query-fn]} section_id section]
  (query-fn :update-section {:section_id section_id :section section}))
(defn move-section [{:keys [query-fn]} mid]
  (query-fn :move-section {:mid mid}))

(defn get-editor [req question_id]
  (some-> (get-question req question_id)
          :editor
          read-string))

(defn set-editor [{:keys [query-fn]} question_id editor]
  (query-fn :update-editor {:question_id question_id
                            :editor (pr-str editor)}))

(defn get-questions-file [req project_id file_id]
  (->> (get-questions-flat req project_id)
       (map #(update % :editor read-string))
       (filter (fn [{:keys [editor]}]
                 (->> editor :references vals (some #(-> % :file_id (= file_id))))))))

(defn file-referenced? [req project_id file_id]
  (-> (get-questions-file req project_id file_id) empty? not))

(defn get-pending-file [req project_id file_id preferred_question_id preferred_offset]
  (->>
   (get-questions-flat req project_id)
   (mapcat
    (fn [{:keys [question_id question editor]}]
      (let [{:keys [text references]} (read-string editor)]
        (->> references
             (sort-by first)
             (map-indexed
              (fn [index [_ reference]]
                (mk-assoc reference
                          index reference question_id question text)))
             (filter :migration-pending?)))))
   (sort-by
    (fn [{:keys [question_id offset]}]
      (if (= preferred_question_id question_id)
        (if (= preferred_offset offset) 0 1)
        2)))))

(defn- update-editor [req question_id f & args]
  (set-editor
   req
   question_id
   (apply f (get-editor req question_id) args)))

(defn- update-editors [req project_id f & args]
  (doseq [{:keys [question_id editor]} (get-questions-flat req project_id)]
    (as-> editor e
          (read-string e)
          (apply f e args)
          (set-editor req question_id e))))

(defn- remove-val [x] #(when (not= % x) %))
(defn nullify-file-references [req project_id file_id]
  (update-editors
   req
   project_id
   (fn [editor]
     (->> editor
          :references
          (util/map-vals #(update % :file_id (remove-val file_id)))
          (assoc editor :references)))))

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
  (->> (file/fragment-line req reference)
       (assoc reference :line)
       (update-editor req question_id assoc-in [:references (:offset reference)])))

(defn assoc-reference-page [req
                            question_id
                            offset
                            file_id
                            page]
  (update-editor req
                 question_id
                 assoc-in
                 [:references offset]
                 (mk offset file_id page)))

(defn assoc-reference-file [req
                            question_id
                            offset
                            file_id]
  (update-editor req
                 question_id
                 assoc-in
                 [:references offset]
                 (mk offset file_id)))
