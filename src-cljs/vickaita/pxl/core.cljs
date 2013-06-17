(ns vickaita.pxl.core
  (:require [domina :as dom]
            [domina.events :as evt]
            [goog.dom.ViewportSizeMonitor]
            [clojure.browser.repl :as repl]
            [vickaita.pxl.util :refer [log]]
            [vickaita.pxl.view :as view]
            [vickaita.pxl.image-node :refer [image-node]]
            [vickaita.pxl.app :refer [add-node set-current get-current empty-app]]
            [vickaita.raster.core :as ras :refer [image-data]]
            [vickaita.raster.filters :as filt]))  

(def app-state (atom empty-app))

(defn- open-file-picker
  []
  (.click (first (dom/nodes (dom/by-class "file-picker-input"))))
  nil)

(defn load-image-from-file
  [file]
  (when file
    (let [img (js/Image.)]
      (set! (.-onload img) #(let [img-node (image-node (image-data img) nil {:id :root})]
                              (swap! app-state add-node img-node)
                              (swap! app-state set-current img-node)
                              (set! (.-onload img) nil)))
      (set! (.-src img) (js/URL.createObjectURL file)))))

(def tool-map
  {"t1" {:id "t1" :text "Load an Image"  :transform (fn [_] (open-file-picker)) :control nil}
   "t2" {:id "t2" :text "Invert"         :transform filt/invert     :control nil}
   "t3" {:id "t3" :text "Blur"           :transform filt/blur       :control nil}
   "t4" {:id "t4" :text "Desaturate"     :transform filt/desaturate :control nil}
   ;"t5" {:id "t5" :text "Sobel (broken)" :transform filt/sobel      :control nil}
   "t6" {:id "t6" :text "Sharpen"        :transform filt/sharpen    :control nil}
   "t7" {:id "t7" :text "Brighten"       :transform filt/brighten   :control [{:type "range" :min 0 :max 255 :value 0}]}})

;; Controller

(defn apply-tranform
  [tool-id]
  (when-let [tool (get-in @app-state [:tools tool-id])]
    (when-let [image ((:transform tool) (get-current @app-state))]
      (let [old-node (get-current @app-state)
            new-node (image-node image tool old-node)]
        (swap! app-state #(-> %
                              (add-node new-node)
                              (set-current new-node)))))))

(defn update-transform-parameters
  [parameters]
  (let [current-node (get-current @app-state)
        current-control (get-in current-node [:tool :control])
        control (doall (map #(assoc %1 :value %2) current-control parameters))
        transform (get-in current-node [:tool :transform])
        parent (get-in @app-state [:graph :nodes (:parent current-node)])
        image (apply transform (conj parameters parent))
        node (image-node image (assoc (:tool current-node) :control control) (get-in @app-state [:graph :nodes (:parent current-node)]))]
    (swap! app-state #(-> %
                          (add-node node)
                          (set-current node)))))

(defn serialize-form
  [form]
  (let [inputs (.getElementsByTagName form "input")]
    (vec (for [input inputs] (.-value input)))))

(defn monitor-app
  []
  (add-watch app-state :app-change view/draw-app!))

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
                       (swap! app-state set-current node))))))
  (evt/listen! (dom/by-id "control") :change
               (fn [e]
                 (evt/prevent-default e)
                 (evt/stop-propagation e)
                 (let [params (serialize-form (evt/current-target e))]
                   (update-transform-parameters params))))
  #_(evt/listen! :keydown #(log "keydown"))
  #_(evt/listen! :keyup #(log "keyup")))

(defn- main
  []
  (repl/connect "http://localhost:9201/repl")
  (monitor-app)
  (monitor-dom)
  (swap! app-state assoc :tools tool-map))

;; Kickoff the main function once the page loads
(evt/listen! js/document "DOMContentLoaded" main)

(comment 

  (get-in @app-state [:graph :nodes (:current @app-state) :tool :control])

  (:current @app-state)

  (:nodes (:graph @app-state))

  (get-in @app-state [:graph :nodes (:current @app-state) ])

  (serialize-form (.getElementById js/document "control"))

  (map #(assoc % :value %2)
       (get-in @app-state [:graph :nodes (:current @app-state) :tool :control])
       '("15"))

  )
