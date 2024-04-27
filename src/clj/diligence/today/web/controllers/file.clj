(ns diligence.today.web.controllers.file
    (:require
      [clojure.java.io :as io]
      [clojure.java.shell :refer [sh]])
    (:import
      java.io.File))

(def files (File. "files"))
(.mkdirs (File. "files/thumbnails"))

(defn copy-file [{:keys [query-fn]} question_id {:keys [tempfile filename]}]
  (let [f (File. files filename)]
    (when-not (.exists f)
              (io/copy tempfile f)
              (sh "convert"
                  (format "files/%s[0]" filename)
                  (format "files/thumbnails/%s"
                          (.replace filename ".pdf" ".jpg"))))
    (query-fn :insert-file {:question_id question_id :filename filename})))

(defn get-files [{:keys [query-fn]} question_id]
  (query-fn :get-files {:question_id question_id}))

(defn get-file [{:keys [query-fn]} file_id]
  (query-fn :get-file {:file_id file_id}))

(defn get-file-stream [req file_id]
  (let [{:keys [filename]} (get-file req file_id)]
    (io/input-stream (str "files/" filename))))

(defn get-thumbnail-stream [req file_id]
  (let [{:keys [filename]} (get-file req file_id)]
    (->> (.replace filename ".pdf" ".jpg")
         (str "files/thumbnails/")
         io/input-stream)))
