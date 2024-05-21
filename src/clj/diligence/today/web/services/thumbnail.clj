(ns diligence.today.web.services.thumbnail
    (:require
      [clojure.java.shell :refer [sh]]
      [diligence.today.web.services.file-locator :as file-locator])
    (:import
      java.io.File))

(defn thumbnails [project_id dir index]
  (-> (file-locator/thumbnail-parent project_id dir index)
      File.
      .mkdirs)
  (sh "convert"
      (str (file-locator/pdf-triple project_id dir index) "[0]")
      (file-locator/thumbnail-file project_id dir index 0))
  (future
   (sh "convert"
       (file-locator/pdf-triple project_id dir index)
       (file-locator/thumbnail-all project_id dir index))))
