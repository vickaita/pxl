(ns vickaita.pxl.image-node
  (:require [vickaita.raster.core :as raster]
            [vickaita.pxl.util :refer [log]]))

(def ^:private id-counter (atom 0))

(defn- guid [] (str "image-node-" (swap! id-counter inc)))

(defrecord ImageNode [parent id width height data tool])

;; ImageNode
{:tool {:id "tool-1" :control []}
 :parameters [10 23 14]
 :partitions {:complete #{} :remaining #{}}

 :width 1
 :height 1
 :data [0 0 0 0]

 :id "image-node-2" 
 :hash "jifj9wjflksjfew"

 :parent-id "image-node-1" 
 :parent-hash "1g6312t3gasdfhenfin"}

(defn image-node
  ([data] (image-node data nil {:id :root}))
  ([data tool parent]
   {:parent (:id parent)
    :id (guid)
    :width (raster/width data)
    :height (raster/height data)
    :data (raster/data data)
    :tool tool}))

;(defn )
;   when-let [image (apply (:transform tool) (conj params (:workspace @app-state)))]
;

(defprotocol IImageNode
  (data [this f]))
