(ns scripts.simplify-svg
    (:require
      [clojure.string :as string])
    (:import
      java.text.DecimalFormat))

(defn- map-rest [f [x & rest]]
  (conj
   (map f rest)
   x))

(defn parse-float [s]
  (try
    (Double/parseDouble s)
    (catch NumberFormatException e
      (throw (Exception. s)))))

(def formatter (DecimalFormat. "#.##"))
(def pr-num #(.format formatter %))

(defn- split-line [s]
  (map-rest parse-float (.split s " ")))

(defn data [] (-> "resources-other/d.txt"
                  slurp
                  (.split "\n")
                  (->> (map split-line))))

(defn reduce2 [f s]
  (reduce
   (fn [s x]
     (conj s
           (f (peek s) x)))
   []
   s))

(defn- multiple? [a b]
  (and
   (zero? (mod a b))
   (pos? (/ a b))))

(defn- data-length [type]
  (case (.toLowerCase type)
        "m" 2
        "l" 2
        "c" 6
        "h" 1
        "a" 7
        "v" 1))

(defn- assert-length [[type & data :as line]]
  (assert (multiple? (count data) (data-length type)) line)
  line)

(defn- split-vector [[type & data :as line]]
  (->> data
       (partition (data-length type))
       (map-indexed
        (fn [i x]
          (conj x
                (if (zero? i)
                  type
                  (case type
                        "m" "l"
                        "M" "L"
                        type)))))))

(defn absolutize [[_ v1]
                  [type & data]]
  (case type
        "M"
        [(list* "M" data) (take-last 2 data)]
        "m"
        (let [adjusted (map + v1 data)]
          [(list* "M" adjusted) adjusted])
        "L"
        [(list* "L" data) (take-last 2 data)]
        "l"
        (let [adjusted (map + v1 data)]
          [(list* "L" adjusted) adjusted])
        "C"
        [(list* "C" data) (take-last 2 data)]
        "c"
        (let [adjusted (map + (cycle v1) data)]
          [(list* "C" adjusted) (take-last 2 adjusted)])
        "H"
        (let [[_ y1] v1
              [x2] data]
          [(list* "H" data) [x2 y1]])
        "h"
        (let [[x1 y1] v1
              [dx] data
              x2 (+ x1 dx)]
          [["H" x2] [x2 y1]])
        "A"
        [(list* "A" data) (take-last 2 data)]
        "a"
        (let [adjusted (map + v1 (take-last 2 data))]
          [(concat ["A"] (drop-last 2 data) adjusted) adjusted])
        "V"
        (let [[x1] v1
              [y2] data]
          [(list* "V" data) [x1 y2]])
        "v"
        (let [[x1 y1] v1
              [dy] data
              y2 (+ y1 dy)]
          [["V" y2] [x1 y2]])))

(defn pr-vector [[data]]
  (->> data
       (map-rest pr-num)
       (string/join " ")))

(defn pr-vectors []
  (->> (data)
       (map assert-length)
       (mapcat split-vector)
       (reduce2 absolutize)
       (map pr-vector)
       (string/join "\n")
       (format (slurp "resources-other/logo.svg"))
       (spit "resources/public/logo.svg")))
