(ns diligence.today.web.controllers.file
    (:require
      [clojure.java.io :as io]
      [clojure.java.shell :refer [sh]]
      [diligence.today.web.services.thumbnail :as thumbnail]
      [diligence.today.util :refer [mk format-js]])
    (:import
      java.io.File))

(def files (File. "files"))

(defn- num-pages [project_id filename]
  (->> (sh "pdftotext" "-f" "10000" (format-js "files/{project_id}/{filename}"))
      :err
       (re-find #"(\d+)\)\.")
       second
       Long/parseLong))

(defn- text-file [project_id filename i]
  (format "files/%s/grep/%s/%03d.txt"
          project_id
          (.replace filename ".pdf" "")
          i))

(defn- convert-page [project_id filename]
  (fn [i]
    (sh "pdftotext"
        "-f" (str i)
        "-l" (str i)
        "-layout"
        (format "files/%s/%s" project_id filename)
        (text-file project_id filename i))))

(defn- convert-pages [project_id filename limit]
  (->> (.replace filename ".pdf" "")
       (format "files/%s/grep/%s" project_id)
       File.
       .mkdirs)
  (->> limit inc (range 1) (map (convert-page project_id filename)) dorun future))

(defn copy-file [{:keys [query-fn]} project_id {:keys [tempfile filename]}]
  (let [f (File. files (str project_id "/" filename))]
    (.mkdirs (File. files project_id))
    (io/copy tempfile f)
    (thumbnail/thumbnails project_id filename)
    (let [pages (num-pages project_id filename)]
      (convert-pages project_id filename pages)
      (query-fn :insert-file (mk project_id filename pages)))))

(defn get-files [{:keys [query-fn]} project_id]
  (query-fn :get-files {:project_id project_id}))

(defn get-file [{:keys [query-fn]} file_id]
  (query-fn :get-file {:file_id file_id}))

(defn get-thumbnail-stream [req file_id page]
  (let [{:keys [filename project_id]} (get-file req file_id)]
    (io/input-stream
     (thumbnail/thumbnail-file project_id filename page))))
