(ns user
  "Userspace functions you can run by default in your local REPL."
  (:require
    [clojure.java.io :as io]
    [clojure.pprint]
    [clojure.spec.alpha :as s]
    [clojure.tools.namespace.repl :as repl]
    [criterium.core :as c]                                  ;; benchmarking
    [expound.alpha :as expound]
    [integrant.core :as ig]
    [integrant.repl :refer [clear go halt prep init reset reset-all]]
    [integrant.repl.state :as state]
    [kit.api :as kit]
    [lambdaisland.classpath.watch-deps :as watch-deps]      ;; hot loading for deps
    [diligence.today.core :refer [start-app]]))

;; we want out own sh

(defn sh
  [dir & args]
  (let [proc (.exec (Runtime/getRuntime)
                    ^"[Ljava.lang.String;" (into-array args)
                    (make-array String 0)
                    (io/as-file dir))]
    (with-open [stdout (.getInputStream proc)
                stderr (.getErrorStream proc)]
      (future (io/copy stdout *out*))
      (future (io/copy stderr *err*))
      (.waitFor proc))))

;; uncomment to enable hot loading for deps
(watch-deps/start! {:aliases [:dev :test]})

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(add-tap (bound-fn* clojure.pprint/pprint))

(defn dev-prep!
  []
  (integrant.repl/set-prep! (fn []
                              (-> (diligence.today.config/system-config {:profile :dev})
                                  (ig/prep)))))

(defn test-prep!
  []
  (integrant.repl/set-prep! (fn []
                              (-> (diligence.today.config/system-config {:profile :test})
                                  (ig/prep)))))

;; Can change this to test-prep! if want to run tests as the test profile in your repl
;; You can run tests in the dev profile, too, but there are some differences between
;; the two profiles.
(dev-prep!)

(repl/set-refresh-dirs "src/clj")

(def refresh repl/refresh)

(defn wipe []
  (halt)
  (sh "." "rm" "-r" "today_dev.db" "files")
  (reset))

(defn test-node []
  (wipe)
  (sh "test-integration" "node" "index.js"))

(comment
  (go)
  (reset))
