(ns diligence.today.web.controllers.file
    (:require
      [clojure.java.io :as io]
      [clojure.java.shell :refer [sh]])
    (:import
      java.io.File))

(def files (File. "files"))
(.mkdirs (File. "files/thumbnails"))

(defn copy-file [{:keys [tempfile filename]}]
  (let [f (File. files filename)]
    (when-not (.exists f)
              (io/copy tempfile f)
              (sh "convert"
                  (format "files/%s[0]" filename)
                  (format "files/thumbnails/%s"
                          (.replace filename ".pdf" ".jpg"))))))
