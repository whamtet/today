(ns diligence.today.web.controllers.file
    (:require
      [clojure.java.io :as io]
      [clojure.java.shell :refer [sh]]
      [diligence.today.web.services.thumbnail :as thumbnail]
      [diligence.today.util :refer [mk]])
    (:import
      java.io.File))

(def files (File. "files"))
(.mkdir files)

(defn- num-pages [filename]
  (->> (sh "pdftotext" "-f" "10000" (str "files/" filename))
      :err
       (re-find #"(\d+)\)\.")
       second
       Long/parseLong))

(defn- grep-dir [filename]
  (->> (.replace filename ".pdf" "")
       (str "files/grep/")
       File.
       .mkdirs))

(defn- text-file [filename i]
  (format "files/grep/%s/%03d.txt"
          (.replace filename ".pdf" "")
          i))

(defn- convert-page [filename]
  (fn [i]
    (sh "pdftotext"
        "-f" (str i)
        "-l" (str i)
        "-layout"
        (str "files/" filename)
        (text-file filename i))))

(defn- convert-pages [filename limit]
  (grep-dir filename)
  (->> limit inc (range 1) (map (convert-page filename)) dorun future))

(defn copy-file [{:keys [query-fn]} project_id {:keys [tempfile filename]}]
  (let [f (File. files filename)]
    (when-not (.exists f)
              (io/copy tempfile f)
              (let [limit (num-pages filename)]
                (convert-pages filename limit)
                (thumbnail/thumbnails filename limit)))
    (query-fn :insert-file (mk project_id filename))))

(defn get-files [{:keys [query-fn]} project_id]
  (query-fn :get-files {:project_id project_id}))

(defn get-file [{:keys [query-fn]} file_id]
  (query-fn :get-file {:file_id file_id}))

(defn get-file-stream [req file_id]
  (let [{:keys [filename]} (get-file req file_id)]
    (io/input-stream (str "files/" filename))))

(defn get-thumbnail-stream [req file_id]
  (let [{:keys [filename]} (get-file req file_id)]
    (io/input-stream
     (thumbnail/thumbnail-file filename 0))))
