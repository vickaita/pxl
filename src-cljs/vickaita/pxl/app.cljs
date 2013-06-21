(ns vickaita.pxl.app
  (:require [vickaita.pxl.image-node :as n :refer [image-node]]
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

(defn render-job
  [node]
  (fn [app write-fn]
    (let [write (comp write-fn (partial n/merge-image-data node))
          transform (get-in node [:tool :transform])
          source-node (get-node app (:parent-id node))
          params (vec (map :value (-> node :tool :control)))
          args (conj params source-node write)
          image (apply transform args)]
      (write image))))

(defn add-render-job
  [app node]
  (update-in app [:render-jobs] conj (render-job node)))

(defn transform
  [app tool-id]
  (when-let [tool (get-in app [:tools tool-id])]
    (let [parent (or (get-current app) {:id :root})
          node (image-node (image-data nil) tool parent)]
      (-> app
          (add-current node)
          (add-render-job node)))))

(defn update-transform-parameters
  [app parameters]
  (let [cur-node (get-current app)
        tool (:tool cur-node)
        control (vec (map #(assoc %1 :value %2) (:control tool) parameters))
        parent (get-node app (:parent-id cur-node)) 
        node (image-node cur-node (assoc tool :control control) parent)]
    (-> app
        (add-current node)
        (add-render-job node))))
