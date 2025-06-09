(ns diligence.today.web.controllers.file
    (:require
      [clojure.string :as string]
      [clojure.java.io :as io]
      [clojure.java.shell :refer [sh]]
      [diligence.today.web.services.diff :as diff]
      [diligence.today.web.services.file-locator :as file-locator]
      [diligence.today.web.services.grep :as grep]
      [diligence.today.web.services.image-hash :as image-hash]
      [diligence.today.web.services.thumbnail :as thumbnail]
      [diligence.today.web.services.wc :as wc]
      [diligence.today.util :refer [mk]])
    (:import
      java.io.File))

(def files (File. "files"))

(defn- num-pages
  "num of pages in pdf"
  [project_id dir index]
  (->> (file-locator/pdf-triple project_id dir index)
       (sh "pdftotext" "-f" "10000")
       :err
       (re-find #"(\d+)\)\.")
       second
       Long/parseLong))

(defn- convert-page [project_id dir index]
  (fn [i]
    (sh "pdftotext"
        "-f" (str (inc i))
        "-l" (str (inc i))
        "-layout"
        (file-locator/pdf-triple project_id dir index)
        (file-locator/grep-file project_id dir index i))))

(defn- convert-all [project_id dir index]
  (sh "pdftotext"
      "-layout"
      (file-locator/pdf-triple project_id dir index)
      (file-locator/diff-file project_id dir index)))

(defn- convert-pages [project_id dir index limit]
  (->> (file-locator/grep-parent project_id dir index)
       File.
       .mkdirs)
  (convert-all project_id dir index)
  (->> limit range (map (convert-page project_id dir index)) dorun)
  (wc/wc! project_id dir index)
  (image-hash/hash! project_id dir index))

(defn new-file [{:keys [query-fn]} project_id {:keys [tempfile filename]}]
  (let [suffix (re-find #".\w+$" filename)
        dir-name (-> tempfile .getName (.split "-") last (.replace ".tmp" ""))
        filename-storage (str "f00" suffix)
        storage-dir (->> dir-name (str project_id "/") (File. files))]
    (.mkdirs storage-dir)
    (io/copy tempfile (File. storage-dir filename-storage))
    (if (= ".pdf" suffix)
      (let [pages (num-pages project_id dir-name 0)]
        (thumbnail/thumbnails project_id dir-name 0)
        (future
         (convert-pages project_id dir-name 0 pages))
        (query-fn :insert-file {:project_id project_id
                                :dir dir-name
                                :filename_original filename
                                :pages pages}))
      ;; just save entry
      (query-fn :insert-file {:project_id project_id
                              :dir dir-name
                              :filename_original filename
                              :pages nil}))))

(defn get-file [{:keys [query-fn]} file_id]
  (as-> (query-fn :get-file {:file_id file_id}) m
        (assoc m :index (:ind m))
        (dissoc m :ind)))

(defn copy-file [{:keys [query-fn] :as req} project_id {:keys [tempfile filename]} file_id]
  (let [{:keys [filename_original dir] :as saved} (get-file req file_id)
        suffix (re-find #".\w+$" filename_original)
        index (-> saved :index inc)]
    (assert (.endsWith filename suffix))
    (io/copy tempfile (File. (file-locator/pdf-triple project_id dir index)))
    (thumbnail/thumbnails project_id dir index)
    (let [pages (num-pages project_id dir index)]
      (convert-pages project_id dir index pages)
      (query-fn :update-file (mk index filename pages file_id)))))

(defn get-files [{:keys [query-fn]} project_id]
  (query-fn :get-files {:project_id project_id}))

(defn empty-files? [req project_id]
  (empty?
   (get-files req project_id)))

(defn get-thumbnail-stream [req file_id page]
  (let [{:keys [project_id dir index]} (get-file req file_id)]
    (io/input-stream
     (file-locator/thumbnail-file project_id dir index page))))

(defn suffix->mime [suffix]
  (case suffix
        "csv" "text/csv"
        "xlsx" "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        "pdf" "application/pdf"))
(defn get-file-stream [req file_id]
  (let [{:keys [project_id dir index filename_original]} (get-file req file_id)
        suffix (last (.split filename_original "\\."))]
    [(suffix->mime suffix)
     (io/input-stream
      (file-locator/file-quadruple project_id dir index suffix))]))

(defn fragment-line [req {:keys [file_id page fragment]}]
  (let [{:keys [project_id dir index]} (get-file req file_id)]
    (grep/fragment-line project_id dir index page fragment)))

(defn whole-page [req file_id page]
  (let [{:keys [project_id dir index pages_old pages]} (get-file req file_id)]
    (grep/whole-page project_id dir index pages_old pages page)))

(defn- move-line* [project_id dir index page line]
  (some->> (wc/convert project_id dir (dec index) page line)
           (diff/new-line project_id dir (dec index))
           (wc/convert project_id dir index)))

(defn move-line [req file_id page line]
  (let [{:keys [project_id dir index]} (get-file req file_id)]
    (assert (pos? index))
    (move-line* project_id dir index page line)))

(defn delete-file [{:keys [query-fn]} project_id file_id]
  (let [[{:keys [dir]}] (query-fn :delete-file {:file_id file_id})
        storage-dir (->> dir (str project_id "/") (File. files))]
    (sh "rm" "-r" (str storage-dir))))
