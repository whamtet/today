(ns diligence.today.web.controllers.project)

(defn create-project [{:keys [query-fn]} project-name]
  (query-fn :create-project {:project-name project-name}))
