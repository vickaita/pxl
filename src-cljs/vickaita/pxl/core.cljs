(ns vickaita.pxl.core
  (:require [domina :as dom]
            [domina.events :as evt]
            [goog.dom.ViewportSizeMonitor]
            [clojure.browser.repl :as repl]
            [vickaita.pxl.render :as render]
            [vickaita.pxl.image-node :refer [image-node]]
            [vickaita.raster.core :as ras :refer [image-data]]
            [vickaita.raster.filters :as filt]))  

;; Model

(def empty-app
  {:graph {:nodes {} 
           :heads #{}
           :roots #{}
           :selected #{}}
   :workspace {}
   :tools {}
   :settings {:graph-visible true
              :tools-visible true}})

(defn add-node
  "Add node `n` to graph `g`."
  [app node]
  (-> app 
      (update-in [:graph :heads] disj (:parent node))
      (update-in [:graph :heads] conj (:id node))
      (update-in [:graph :nodes] assoc (:id node) node)
      (assoc-in [:workspace] node)))

(def app-state (atom empty-app))

;; ---

(defn- open-file-picker
  []
  (.click (first (dom/nodes (dom/by-class "file-picker-input"))))
  nil)

(defn load-image-from-file
  [file]
  (when file
    (let [img (js/Image.)]
      (set! (.-onload img) #(do (swap! app-state add-node (image-node (image-data img)))
                                (set! (.-onload img) nil)))
      (set! (.-src img) (js/URL.createObjectURL file)))))

(def tool-map
  {"t1" {:id "t1" :text "Load an Image"  :transform (fn [_] (open-file-picker))}
   "t2" {:id "t2" :text "Invert"         :transform filt/invert}
   "t3" {:id "t3" :text "Blur"           :transform filt/blur}
   "t4" {:id "t4" :text "Desaturate"     :transform filt/desaturate}
   "t5" {:id "t5" :text "Sobel (broken)" :transform filt/sobel}
   "t6" {:id "t6" :text "Sharpen"        :transform filt/sharpen}})

;; View

(defn monitor-models
  []
  (add-watch app-state :app-change (fn [_ _ _ n] (render/draw-app! n))))

;; Controller

(defn apply-tranform
  [tool-id]
  (when-let [tool (get-in @app-state [:tools tool-id])]
    (when-let [image ((:transform tool) (:workspace @app-state))]
      (let [old-node (:workspace @app-state)
            new-node (image-node image old-node)]
        (swap! app-state add-node new-node)))))

(defn update-transform-parameters
  [parameters]
  (let [old-node (:workspace @app-state)
        new-node (assoc old-node :parameters parameters)]
    (swap! app-state assoc :workspace new-node)))

;;; ImageNode
;{
; :transform curves
; :parmeters [10 23 14]}
; :width 1
; :height 1
; :data [0 0 0 0]
; :hash "jifj9wjflksjfew"
; :parent-hash "1g6312t3gasdfhenfin"
; :parent-id "image-node-1" 
; :id "image-node-2"
; }

(defn monitor-dom
  []
  (let [file-picker (first (dom/nodes (dom/by-class "file-picker-input")))]
    (evt/listen! file-picker :change
                 (fn [e]
                   (evt/prevent-default e)
                   (evt/stop-propagation e)
                   (load-image-from-file (aget file-picker "files" 0))))) 
  (evt/listen! (dom/by-class "tools") :click
               (fn [e]
                 (evt/prevent-default e)
                 (evt/stop-propagation e)
                 (when-let [tool-id (dom/attr (evt/target e) :id)]
                   (apply-tranform tool-id))))
  (evt/listen! (dom/by-id "graph") :click
               (fn [e]
                 (evt/prevent-default e)
                 (evt/stop-propagation e)
                 (let [element (evt/target e)]
                   (when (dom/has-class? element "image-node")
                     (when-let [node (get-in @app-state [:graph :nodes (dom/attr element :id)])]
                       (swap! app-state assoc :workspace node))))))
  #_(evt/listen! :keydown #(log "keydown"))
  #_(evt/listen! :keyup #(log "keyup")))

(defn- main
  []
  (repl/connect "http://localhost:9201/repl")
  (monitor-models)
  (monitor-dom)
  (swap! app-state assoc :tools tool-map))

;; Kickoff the main function once the page loads
(evt/listen! js/document "DOMContentLoaded" main)
