(ns diligence.today.web.services.file-locator
    (:require
      [clojure.string :as string]))

;; (re-seq #"\{([^\}]+)}" s)
(defn- get-syms [[_ s]]
  (if (symbol? s) [s] (rest s)))

(defmacro defformat [symbol s]
  (let [bindings (->> s (re-seq #"\{([^}]+)}") (map #(update % 1 read-string)))]
    `(defn ~symbol ~(->> bindings (mapcat get-syms) distinct vec)
      (-> ~s
          ~@(for [[s binding] bindings]
             `(string/replace-first ~s ~binding))))))

;; utility functions
(defn- pdf-file [index]
  (format "f%02d.pdf" index))

(defformat pdf-triple "files/{project_id}/{dir}/{(pdf-file index)}")

(defn- general-file [index suffix]
  (format "f%02d.%s" index suffix))

(defformat file-quadruple "files/{project_id}/{dir}/{(general-file index suffix)}")

(defn- grep-dir [index]
  (format "grep%02d" index))
(defn- grep-file* [index]
  (format "%03d.txt" index))

(defformat grep-parent "files/{project_id}/{dir}/{(grep-dir index)}")
(defformat grep-file "files/{project_id}/{dir}/{(grep-dir index)}/{(grep-file* page)}")

(defn- diff-file* [index]
  (format "all%02d.txt" index))

(defformat diff-file "files/{project_id}/{dir}/{(diff-file* index)}")

(defn- ihash [page]
  (format "%03d.ihash" page))

(defformat image-hash "files/{project_id}/{dir}/{(grep-dir index)}/{(ihash page)}")

(defn- thumbnail-index [index]
  (format "thumbnail%02d" index))
(defn- img-index [page]
  (format "%03d.jpg" page))

(defformat thumbnail-parent "files/{project_id}/{dir}/{(thumbnail-index index)}")
(defformat thumbnail-file "files/{project_id}/{dir}/{(thumbnail-index index)}/{(img-index page)}")
(defformat thumbnail-all "files/{project_id}/{dir}/{(thumbnail-index index)}/%03d.jpg")

(defn- wc-file* [index]
  (format "%02d.wc" index))
(defformat wc-src "files/{project_id}/{dir}/{(grep-dir index)}/*.txt")
(defformat wc-file "files/{project_id}/{dir}/{(wc-file* index)}")
