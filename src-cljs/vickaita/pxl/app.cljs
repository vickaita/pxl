(ns vickaita.pxl.app
  (:require [vickaita.pxl.image-node :refer [image-node]]
            [vickaita.raster.core :refer [image-data width height data]]
            [vickaita.pxl.util :refer [log]]))

(def empty-app
  {:graph {:nodes {} 
           ;:by-parent {}
           ;:heads #{}
           ;:roots #{}
           ;:selected #{}
           } 
   :current {}
   :tools {}
   :render-jobs []
   :settings {:graph-visible true
              :tools-visible true}})

(defn add-node
  "Add an image-node to app :graph"
  [app node]
  (-> app 
      #_(update-in [:graph :heads] disj (:parent-id node))
      #_(update-in [:graph :heads] conj (:id node))
      (update-in [:graph :nodes] assoc (:id node) node)))

(defn get-node
  [app node-id]
  (get-in app [:graph :nodes node-id]))

(defn set-node
  [app node]
  (assoc-in app [:graph :nodes (:id node)] node))

(defn get-current
  [app]
  (get-in app [:graph :nodes (:current app)]))

(defn set-current
  [app node]
  (assoc app :current (:id node)))

(defn add-current
  [app node]
  (-> app
      (add-node node)
      (set-current node)))

(defn add-render-job
  [app job]
  (update-in app [:render-jobs] conj job))

(defn merge-image-data
  [node image]
  (-> node
      (assoc :width (width image))
      (assoc :height (height image))
      (assoc :data (data image))))

(defn render-job
  [node]
  (fn [app write-fn]
    (let [write (comp write-fn (partial merge-image-data node))
          transform (get-in node [:tool :transform])
          source-node (get-node app (:parent-id node))
          args (conj [] source-node write)
          image (apply transform args)]
      (write image))))

(defn transform
  [app tool-id]
  (when-let [tool (get-in app [:tools tool-id])]
    (let [parent (or (get-current app) {:id :root})
          node (image-node (image-data nil) tool parent)]
      (-> app
          (add-current node)
          (add-render-job (render-job node))))))

(defn update-transform-parameters
  [app parameters]
  (let [current-node (get-current app)
        current-control (get-in current-node [:tool :control])
        control (doall (map #(assoc %1 :value %2) current-control parameters))
        transform (get-in current-node [:tool :transform])
        parent (get-in app [:graph :nodes (:parent-id current-node)])
        image (apply transform (conj parameters parent))
        node (image-node image (assoc (:tool current-node) :control control) (get-in app [:graph :nodes (:parent-id current-node)]))]
    (-> app
        (add-node node)
        (set-current node))))
