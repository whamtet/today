(ns diligence.today.web.services.thumbnail
    (:require
      [clojure.java.shell :refer [sh]]
      [diligence.today.util :refer [format-js]])
    (:import
      java.io.File))

(defn thumbnail-file [project_id filename i]
  (format "files/%s/thumbnails/%s/%03d.jpg"
          project_id
          (.replace filename ".pdf" "")
          i))
(defn- thumbnail-all [project_id filename]
  (format "files/%s/thumbnails/%s/%%03d.jpg"
          project_id
          (.replace filename ".pdf" "")))

(defn thumbnails [project_id filename]
  (->> (.replace filename ".pdf" "")
       (format "files/%s/thumbnails/%s" project_id)
       File.
       .mkdirs)
  ;; convert first image now
  (sh "convert" (format-js "files/{project_id}/{filename}[0]") (thumbnail-file project_id filename 0))
  (future
   (sh "convert" (format-js "files/{project_id}/{filename}") (thumbnail-all project_id filename))))
