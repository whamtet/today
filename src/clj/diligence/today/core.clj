(ns diligence.today.core
  (:require
    [clojure.tools.logging :as log]
    [integrant.core :as ig]
    [diligence.today.config :as config]
    [diligence.today.env :refer [defaults]]
    simpleui.config

    ;; Edges
    [kit.edge.server.undertow]
    [diligence.today.web.handler]

    ;; Routes
    [diligence.today.web.routes.api]

    [diligence.today.web.routes.ui]
    [kit.edge.db.sql.conman]
    [diligence.today.migratus])
  (:gen-class))

;; log uncaught exceptions in threads
(Thread/setDefaultUncaughtExceptionHandler
  (reify Thread$UncaughtExceptionHandler
    (uncaughtException [_ thread ex]
      (log/error {:what :uncaught-exception
                  :exception ex
                  :where (str "Uncaught exception on" (.getName thread))}))))

(defonce system (atom nil))

(defn stop-app []
  ((or (:stop defaults) (fn [])))
  (some-> (deref system) (ig/halt!))
  (shutdown-agents))

(defn start-app [& [params]]
  ((or (:start params) (:start defaults) (fn [])))
  (->> (config/system-config (or (:opts params) (:opts defaults) {}))
       (ig/prep)
       (ig/init)
       (reset! system))
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app)))

(defn -main [& _]
  (start-app))

(simpleui.config/set-render-oob true)
(simpleui.config/set-render-safe false)
