(ns vickaita.pxl.image-node
  (:require [vickaita.raster.core :as raster :refer [width height data]]
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

(defn hash-code
  [node callback]
  ;; TODO
  ())

(defn image-node
  [img-data tool parent]
  (let [w (width img-data)
        h (height img-data)]
    {:parent-id (:id parent)
     :parent-hash-code nil
     :id (next-id)
     :hash-code nil
     :width w
     :height h
     :data (data img-data)
     :tool tool
     :unprocessed-regions [[0 0 w h]]}))
