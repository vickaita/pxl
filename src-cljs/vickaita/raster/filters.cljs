(ns vickaita.raster.filters
  (:require-macros [vickaita.raster.macros :refer [dopixels]])
  (:require [vickaita.raster.core :as c :refer [image-data width height data]]
            [vickaita.raster.geometry :refer [surround]]))

(defn invert
  [img]
  (dopixels [[r g b a] img]
            [(- 255 r) (- 255 g) (- 255 b) a]))

(defn desaturate
  [img]
  (dopixels [[r g b a] img
             :let [avg (/ (+ r g b) 3)]]
            [avg avg avg a]))

(defn blur
  [img]
  (c/convolve [0 1 0
               1 1 1
               0 1 0] 5 0 img))

(defn sobel
  [img]
  (->> img
       (c/convolve [1 0 -1
                    2 0 -2
                    1 0 -1] 1 0)
       (c/convolve [ 1  2  1
                     0  0  0
                    -1 -2 -1] 1 0)))

(defn no-alpha
  [matrix]
  (let [center (Math/floor (/ (count matrix) 2))
        v (matrix center)]
    (assoc (vec (map #(vector % % % 0) matrix)) center [v v v 1])))

(defn foo
  [img]
  (c/convolve (no-alpha [-1 -1 -1
                         -1  8 -1
                         -1 -1 -1]) 1 0 img))

(defn sharpen
  [img]
  (c/convolve (no-alpha [1  1 1
                         1 -7 1
                         1  1 1]) 1 0 img))

(defn brighten
  ([img] (brighten 0 img))
  ([amount img]
   (let [amt (js/parseInt amount 10)]
     (dopixels [[r g b a] img]
               [(+ r amt) (+ g amt) (+ b amt) a]))))
