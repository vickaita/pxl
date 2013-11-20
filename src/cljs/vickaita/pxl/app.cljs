(ns vickaita.pxl.app
  (:require [cljs.core.async :refer [put! chan >! <!]]
            [vickaita.pxl.image-node :as n :refer [image-node render-job]]
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
  (get-in app [:graph :nodes (:current app)] {:id :root :width 0 :height 0 :data nil}))

(defn set-current
  [app node]
  (assoc app :current (:id node)))

(defn add-current
  [app node]
  (-> app
      (add-node node)
      (set-current node)))

(defn subdivide
  [[min-x min-y w h]]
  (if (or (= w 0) (= h 0))
    [[0 0 0 0]]
    (let [step 250 
          max-x (+ min-x w)
          max-y (+ min-y h)]
      (for [x (range min-x max-x step) y (range min-y max-y step)]
        [x y (Math/min max-x (+ step x)) (Math/min max-y (+ step y))]))))

(defn add-render-job
  [app node]
  (let [w (width node)
        h (height node)]
    (update-in app [:render-jobs] concat
               (map (partial render-job node) (subdivide [0 0 w h])))))

(defn transform
  [app tool-id]
  (when-let [tool (get-in app [:tools tool-id])]
    (let [node (n/create-child (get-current app) tool)]
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

(defn job-channel
  [app]
  (let [c (chan)])
  (add-watch app :job-channel
             (fn [k r o n]
                                
                                ))
  )

(defn process-job
  [job]
  (.log js/console "yo"))
