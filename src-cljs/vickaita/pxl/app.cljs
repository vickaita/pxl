(ns vickaita.pxl.app
  (:require))

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

