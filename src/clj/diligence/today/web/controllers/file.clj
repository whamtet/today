(ns diligence.today.web.controllers.file
    (:require
      [clojure.java.io :as io]
      [clojure.java.shell :refer [sh]])
    (:import
      java.io.File))

(def files (File. "files"))
(.mkdirs (File. "files/thumbnails"))

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

(defn- convert-pages [filename]
  (grep-dir filename)
  (->> filename num-pages inc (range 1) (map (convert-page filename)) dorun))

(defn copy-file [{:keys [query-fn]} question_id {:keys [tempfile filename]}]
  (let [f (File. files filename)]
    (when-not (.exists f)
              (io/copy tempfile f)
              (convert-pages filename)
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
