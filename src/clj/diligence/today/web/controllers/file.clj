(ns diligence.today.web.controllers.file
    (:require
      [clojure.string :as string]
      [clojure.java.io :as io]
      [clojure.java.shell :refer [sh]]
      [diligence.today.web.services.diff :as diff]
      [diligence.today.web.services.grep :as grep]
      [diligence.today.web.services.image-hash :as image-hash]
      [diligence.today.web.services.thumbnail :as thumbnail]
      [diligence.today.web.services.wc :as wc]
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

(defn- convert-page [project_id filename]
  (fn [i]
    (sh "pdftotext"
        "-f" (str (inc i))
        "-l" (str (inc i))
        "-layout"
        (format "files/%s/%s" project_id filename)
        (grep/grep-file project_id filename i))))

(defn- convert-all [project_id filename]
  (sh "pdftotext"
      "-layout"
      (format "files/%s/%s" project_id filename)
      (diff/diff-file project_id filename)))

(defn- convert-pages [project_id filename limit]
  (->> (.replaceAll filename ".pdf$" "")
       (format "files/%s/grep/%s" project_id)
       File.
       .mkdirs)
  (convert-all project_id filename)
  (->> limit range (map (convert-page project_id filename)) dorun)
  (wc/wc! project_id filename)
  (image-hash/hash! project_id filename))

;; todo: generate our own storage filenames
(defn- index-filename [project_id filename i]
  (assert (.endsWith filename ".pdf"))
  (let [truncated (.replaceAll filename ".pdf$" "")
        new-filename (->> i (format ".%02d.pdf") (str truncated))]
    (if (.exists (File. files (str project_id "/" new-filename)))
      (recur project_id filename (inc i))
      [new-filename i])))

(def suffix-match #"(\d+).pdf$")
(defn dec-filename-index [filename]
  (->> filename
       (re-find suffix-match)
       second
       Long/parseLong
       dec
       (format "%02d.pdf")
       (string/replace filename suffix-match)))

(defmacro if-index [body]
  `(if (zero? ~'index)
    (future ~body)
    ~body))
(defn copy-file [{:keys [query-fn]} project_id {:keys [tempfile filename]} old-filename]
  (let [[filename index] (index-filename project_id (or old-filename filename) 0)]
    (.mkdirs (File. files (str project_id)))
    (io/copy tempfile (File. files (str project_id "/" filename)))
    (thumbnail/thumbnails project_id filename)
    (let [pages (num-pages project_id filename)]
      (if-index (convert-pages project_id filename pages))
      (if (pos? index)
        (-> (query-fn :update-file {:filename filename
                                    :pages pages
                                    :old-filename (dec-filename-index filename)})
            first
            :file_id)
        (do
          (query-fn :insert-file (mk project_id filename pages))
          nil)))))

(defn get-files [{:keys [query-fn]} project_id]
  (query-fn :get-files {:project_id project_id}))

(defn empty-files? [req project_id]
  (empty?
   (get-files req project_id)))

(defn get-file [{:keys [query-fn]} file_id]
  (query-fn :get-file {:file_id file_id}))

(defn get-thumbnail-stream [req file_id page]
  (let [{:keys [filename project_id]} (get-file req file_id)]
    (io/input-stream
     (thumbnail/thumbnail-file project_id filename page))))

(defn get-file-stream [req file_id]
  (let [{:keys [filename project_id]} (get-file req file_id)]
    (io/input-stream
     (format-js "files/{project_id}/{filename}"))))

(defn fragment-line [req {:keys [file_id page fragment]}]
  (let [{:keys [filename project_id]} (get-file req file_id)]
    (grep/fragment-line project_id filename page fragment)))

(defn whole-page [req file_id page]
  (let [{:keys [filename project_id pages pages_old]} (get-file req file_id)]
    (grep/whole-page project_id (dec-filename-index filename) pages_old filename pages page)))

(defn move-line [req file_id page line]
  (let [{:keys [filename project_id]} (get-file req file_id)]
    (some->> (wc/convert project_id filename page line)
             (diff/new-line project_id (dec-filename-index filename) filename)
             (wc/convert project_id filename))))
