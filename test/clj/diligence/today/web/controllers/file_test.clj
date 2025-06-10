(ns diligence.today.web.controllers.file-test
  (:require
    [clojure.java.shell :refer [sh]]
    [clojure.test :refer :all]
    [diligence.today.web.controllers.file :as file])
  (:import
    java.io.File))

(def req {:query-fn (constantly nil)})
(def project_id 99)
(def tempfile (File. "test-integration/files/simple.pdf"))
(def filename "simple.pdf")
(def parent-dir (File. "files/99/simple.pdf"))

(deftest example-test
  (sh "rm" "-r" "files/99")
  (file/new-file req project_id {:tempfile tempfile :filename filename})
  (file/await-conversion!)
  (let [files (->> parent-dir file-seq (filter #(.isFile %)))]
    (is (= 9 (count files)))))
