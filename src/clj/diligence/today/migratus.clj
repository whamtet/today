(ns diligence.today.migratus
    (:require
      [integrant.core :as ig]
      [migratus.core :as migratus]))

(defmethod ig/init-key :db.sql/migrations
           [_ {:keys [migrate-on-init?]
               :or   {migrate-on-init? true}
               :as   component}]
           (def config component)
           (when migrate-on-init?
                 (migratus/migrate component))
           component)

(defn rollback []
  (migratus/rollback config))

(def create (partial migratus/create config))
