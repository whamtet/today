(ns diligence.today.web.controllers.iam
    (:require
      [diligence.today.env :refer [dev?]]))

(defmacro when-authorized [& body]
  `(do
    (assert ~'user_id)
    ~@body))

(defn prod-authorized? [user_id]
  (or dev? user_id))

(defmacro when-read? [& body]
  `(when ~'read? ~@body))
(defmacro when-edit? [& body]
  `(when ~'edit? ~@body))
(defmacro when-admin? [& body]
  `(when ~'admin? ~@body))

(defmacro when-read-prod [& body]
  (if dev?
    `(do ~@body)
    `(when ~'read? ~@body)))
(defmacro when-edit-prod [& body]
  (if dev?
    `(do ~@body)
    `(when ~'edit? ~@body)))
(defmacro when-admin-prod [& body]
  (if dev?
    `(do ~@body)
    `(when ~'admin? ~@body)))
