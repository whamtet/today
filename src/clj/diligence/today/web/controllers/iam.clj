(ns diligence.today.web.controllers.iam)

(defmacro when-authorized [& body]
  `(do
    (assert ~'user_id)
    ~@body))
