(ns diligence.today.web.controllers.iam
    (:require
      [diligence.today.env :refer [dev?]]))

(defmacro when-authorized [& body]
  `(do
    (assert ~'user_id)
    ~@body))

(defn prod-authorized! [user_id]
  (assert (or dev? user_id)))
