(ns diligence.today.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[today starting]=-"))
   :start      (fn []
                 (log/info "\n-=[today started successfully]=-"))
   :stop       (fn []
                 (log/info "\n-=[today has shut down successfully]=-"))
   :middleware (fn [handler _] handler)
   :opts       {:profile :prod}})

(def dev? false)
(def prod? true)
