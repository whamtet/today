(ns diligence.today.web.controllers.project)

(defn create-project [{:keys [query-fn]} project-name]
  (query-fn :create-project {:project-name project-name}))

(defn get-projects [{:keys [query-fn]}]
  (query-fn :get-projects {}))
