(ns vickaita.pxl.image-node
  (:require [vickaita.raster.core :as raster :refer [width height data]]
            [vickaita.pxl.util :refer [log]]))

(def ^:private id-counter (atom 0))

(defn- next-id [] (str "image-node-" (swap! id-counter inc)))

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
     :parameters []
     :unprocessed-regions [[0 0 w h]]}))

(defn merge-image-data
  [node image]
  (-> node
      (assoc :width (width image))
      (assoc :height (height image))
      (assoc :data (data image))))
