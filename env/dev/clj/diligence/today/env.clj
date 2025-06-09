(ns diligence.today.env
  (:require
    [clojure.tools.logging :as log]
    [diligence.today.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[today starting using the development or test profile]=-"))
   :start      (fn []
                 (log/info "\n-=[today started successfully using the development or test profile]=-"))
   :stop       (fn []
                 (log/info "\n-=[today has shut down successfully]=-"))
   :middleware wrap-dev
   :opts       {:profile       :dev
                :persist-data? true}})

(def dev? true)
(def prod? false)
(defn host [& strs]
  (apply str "http://localhost:2998" strs))
