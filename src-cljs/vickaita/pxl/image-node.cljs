(ns vickaita.pxl.image-node
  (:require [vickaita.raster.core :as raster :refer [image-data width height data]]
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
     :parameters []}))

(defn merge-image-data
  [node image]
  (let [w (width image)
        h (height image)
        d (data image)]
    (cond-> node
      (> w 0) (assoc :width w)
      (> h 0) (assoc :height h)
      d (assoc :data d))))

(defn create-child
  [node tool]
  (let [w (width node)
        h (height node)
        d (data node)]
    (if (and (> w 0) (> h 0))
      (image-node (image-data w h) tool node)   
      (image-node {:width w :height h :data nil} tool node))))

(defn render-job
  [node region]
  {:parent-node-id (:parent-id node)
   :node-id (:id node)
   :region region
   :function
   (fn [read-node write-node]
     (let [write-image (comp write-node (partial merge-image-data node))
           transform (get-in node [:tool :transform])
           parent-node (read-node (:parent-id node))
           params (vec (map :value (-> node :tool :control)))]
       (transform params (data parent-node) region (width node) (data node) write-image)))})

(merge-image-data {:width 10 :height 20 :data nil} {:data :foo})

(comment

  (def a (image-node (image-data 10 10)))
  
  (def b (create-child a nil))
  
  )
