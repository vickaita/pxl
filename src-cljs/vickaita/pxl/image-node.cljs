(ns vickaita.pxl.image-node
  (:require [vickaita.raster.core :as raster]))

(defn image-node
  ([data] (image-node data nil))
  ([data parent] {:parent parent
                  :image-data data
                  :width (raster/width data)
                  :height (raster/height data)
                  :data (raster/data data)}))

;; TODO
(defn persist-node
  [node]
  (.log js/console "saving node to server"))
