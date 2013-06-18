(ns vickaita.pxl.app
  (:require [vickaita.pxl.image-node :refer [image-node]]))

(def empty-app
  {:graph {:nodes {} 
           :by-parent {}
           :heads #{}
           :roots #{}
           :selected #{}} 
   :current {}
   :tools {}
   :settings {:graph-visible true
              :tools-visible true}})

(defn add-node
  "Add an image-node to app :graph"
  [app node]
  (-> app 
      (update-in [:graph :heads] disj (:parent node))
      (update-in [:graph :heads] conj (:id node))
      (update-in [:graph :nodes] assoc (:id node) node)))

(defn get-node
  [app node-id]
  (get-in app [:graph :nodes node-id]))

(defn get-current
  [app]
  (get-in app [:graph :nodes (:current app)]))

(defn set-current
  [app node]
  (assoc app :current (:id node)))

(defn apply-tranform
  [app tool-id]
  (when-let [tool (get-in app [:tools tool-id])]
    (when-let [image ((:transform tool) (get-current app))]
      (let [old-node (get-current app)
            new-node (image-node image tool old-node)]
        (-> app
            (add-node new-node)
            (set-current new-node))))))

(defn update-transform-parameters
  [app parameters]
  (let [current-node (get-current app)
        current-control (get-in current-node [:tool :control])
        control (doall (map #(assoc %1 :value %2) current-control parameters))
        transform (get-in current-node [:tool :transform])
        parent (get-in app [:graph :nodes (:parent current-node)])
        image (apply transform (conj parameters parent))
        node (image-node image (assoc (:tool current-node) :control control) (get-in app [:graph :nodes (:parent current-node)]))]
    (-> app
        (add-node node)
        (set-current node))))
