(ns vickaita.pxl.image-node
  (:require [vickaita.raster.core :as raster]
            [vickaita.pxl.util :refer [log]]))

(def ^:private id-counter (atom 0))

(defn- next-id [] (str "image-node-" (swap! id-counter inc)))

;;; ImageNode
;;; Some notes and ideas about the shape of an image-node.
;{:tool {:id "tool-1" :control []}
; :parameters [10 23 14]
; :partitions {:complete #{} :remaining #{}}
;
; :width 1
; :height 1
; :data [0 0 0 0]
;
; :id "image-node-2" 
; :hash "jifj9wjflksjfew"
;
; :parent-id "image-node-1" 
; :parent-hash "1g6312t3gasdfhenfin"}

(defn image-node
  [data tool parent]
  {:parent (:id parent)
   :id (next-id)
   :width (raster/width data)
   :height (raster/height data)
   :data (raster/data data)
   :tool tool})
