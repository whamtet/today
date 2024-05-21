(ns diligence.today.web.services.file-locator
    (:require
      [clojure.string :as string]))

;; (re-seq #"\{([^\}]+)}" s)
(defn- get-syms [[_ s]]
  (if (symbol? s)
    [s]
    (rest s)))

(defmacro deffile [symbol s]
  (let [bindings (->> s (re-seq #"\{([^\}]+)}") (map #(update % 1 read-string)))]
    `(defn ~symbol ~(->> bindings (mapcat get-syms) distinct vec)
      (-> ~s
          ~@(for [[s binding] bindings]
             `(string/replace-first ~s ~binding))))))

;; utility functions
(defn- pdf-file [index]
  (format "f%02d.pdf" index))

(deffile pdf-triple "files/{project_id}/{dir}/{(pdf-file index)}")

(defn- grep-dir [index]
  (format "grep%02d" index))
(defn- grep-file* [index]
  (format "%03d.txt" index))

(deffile grep-parent "files/{project_id}/{dir}/{(grep-dir index)}")
(deffile grep-file "files/{project_id}/{dir}/{(grep-dir index)}/{(grep-file* page)}")

(defn- diff-file* [index]
  (format "all%02d.txt" index))

(deffile diff-file "files/{project_id}/{dir}/{(diff-file* index)}")

(defn- ihash [page]
  (format "%03d.ihash" page))

(deffile image-hash "files/{project_id}/{dir}/{(grep-dir index)}/{(ihash page)}")

(defn- thumbnail-index [index]
  (format "thumbnail%02d" index))
(defn- img-index [page]
  (format "%03d.jpg" page))

(deffile thumbnail-parent "files/{project_id}/{dir}/{(thumbnail-index index)}")
(deffile thumbnail-file "files/{project_id}/{dir}/{(thumbnail-index index)}/{(img-index page)}")
(deffile thumbnail-all "files/{project_id}/{dir}/{(thumbnail-index index)}/%03d.jpg")

(defn- wc-file* [index]
  (format "%02d.wc" index))
(deffile wc-src "files/{project_id}/{dir}/{(grep-dir index)}/*.txt")
(deffile wc-file "files/{project_id}/{dir}/{(wc-file* index)}")
