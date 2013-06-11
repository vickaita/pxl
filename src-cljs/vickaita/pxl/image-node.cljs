(ns vickaita.pxl.image-node
  (:require [vickaita.raster.core :as raster]
            [vickaita.pxl.util :refer [log]]))

(def ^:private id-counter (atom 0))

(defn- guid [] (str "image-node-" (swap! id-counter inc)))

(defrecord ImageNode [parent id width height data tool])

(defn image-node
  ([data] (image-node data nil))
  ([data parent] {:parent (if parent
                            (or (:id parent) (do (log "no parent")
                                                 (guid)))
                            :root)
                  :id (guid)
                  :width (raster/width data)
                  :height (raster/height data)
                  :data (raster/data data)}))

;; TODO
(defn persist-node
  [node]
  (.log js/console "saving node to server"))
