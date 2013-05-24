(ns vickaita.pxl.image-node
  (:require [vickaita.raster.core :as raster]))

(def id-counter (atom 0))

(defn- guid [] (str "image-node-" (swap! id-counter inc)))

(defn image-node
  ([data] (image-node data nil))
  ([data parent] {:parent (or (:id parent) (guid))
                  :id (guid)
                  :width (raster/width data)
                  :height (raster/height data)
                  :data (raster/data data)}))

;; TODO
(defn persist-node
  [node]
  (.log js/console "saving node to server"))
