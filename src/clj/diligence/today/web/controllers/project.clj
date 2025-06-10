(ns diligence.today.web.controllers.project)

(defn create-project [{:keys [query-fn]} project-name]
  (->
   (query-fn :create-project {:project-name project-name})
   first
   :project_id))

(defn get-projects [{:keys [query-fn]}]
  (query-fn :get-projects {}))

(defn get-random-project [{:keys [query-fn] :as req}]
  (or
   (query-fn :get-random-project {})
   (do
     (create-project req "Sample Project")
     (recur req))))

(defn get-project-by-name [{:keys [query-fn]} new-project-name]
  (query-fn :get-project-by-name {:new-project-name new-project-name}))

(defn get-project-by-id [{:keys [query-fn]} project_id]
  (query-fn :get-project-by-id {:project_id project_id}))

(defn update-project [{:keys [query-fn]} project_id project-name]
  (query-fn :update-project {:name project-name :project_id project_id}))
