(ns the-divine-cheese-code.visualization.svg
  (:require [clojure.string :as clj-str])
  (:refer-clojure :exclude [min max]))

(defn comparator-over-maps
  [comparison-fn ks]
  (fn [maps]
    (zipmap ks
            (map (fn [k] (apply comparison-fn (map k maps)))
                 ks))))

(def min (comparator-over-maps clojure.core/min [:lat :lng]))
(def max (comparator-over-maps clojure.core/max [:lat :lng]))

(defn translate-to-00
  [locations]
  (let [mincoords (min locations)]
    (map #(merge-with - % mincoords) locations)))

(defn scale
  [width height locations]
  (let [maxcoords (max locations)
        ratio {:lat (/ height (:lat maxcoords))
               :lng (/ width (:lng maxcoords))}]
    (map #(merge-with * % ratio) locations)))

(defn latlng->point
  "Convert a lat/lng map to a comma-separated string."
  [latlng]
  ;; I think the following code has transposed the `:lng` and `:lat`
  ;; "functions". Specifically, when I test `points` function, the
  ;; output is transposed compared to the book output. (The book output,
  ;; on page 136 of my Kindle, starts with "50.95,6.97..."; that is,
  ;; latitude and longitude. However, my output is "6.97,50.95...")
  ;; But if I exchange these functions, the overall program is **incorrect**.
  ;; I have put in the "book's mistake" (which I think does something else to
  ;; translate everything back), then the overall program works.
  (str (:lng latlng) "," (:lat latlng)))

(defn points
  "Given a `seq` of lat/lng maps, return string of points joined by space"
  [locations]
  (clj-str/join " " (map latlng->point locations)))

(defn line
  [points]
  (str "<polyline points=\"" points "\" />"))

(defn transform
  "Just chains other functions."
  [width height locations]
  (->> locations
       translate-to-00
       (scale width height)))

(defn xml
  "svg 'template', which also flip the coordinate system"
  [width height locations]
  (str "<svg height=\"" height "\" width=\"" width "\">"
       ;; These two <g> tags flip the coordinate system
       "<g transform=\"translate(0," height ")\">"
       "<g transform=\"scale(1, -1)\">"
       (-> (transform width height locations)
           points
           line)
       "</g></g>"
       "</svg>"
       ))
