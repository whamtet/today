(ns diligence.today.web.services.thumbnail
    (:require
      [clojure.java.shell :refer [sh]])
    (:import
      java.io.File))

(defn thumbnail-file [filename i]
  (format "files/thumbnails/%s/%03d.jpg"
          (.replace filename ".pdf" "")
          i))
(defn- thumbnail-all [filename]
  (format "files/thumbnails/%s/%%03d.jpg"
          (.replace filename ".pdf" "")))

(defn thumbnails [filename limit]
  (->> (.replace filename ".pdf" "")
       (str "files/thumbnails/")
       File.
       .mkdirs)
  ;; convert first image now
  (sh "convert" (format "files/%s[0]" filename) (thumbnail-file filename 0))
  (future
   (sh "convert" (format "files/%s" filename) (thumbnail-all filename))))
