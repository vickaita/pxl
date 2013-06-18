(ns vickaita.pxl.core
  (:require [domina :as dom]
            [domina.events :as evt]
            [goog.dom.ViewportSizeMonitor]
            [clojure.browser.repl :as repl]
            [vickaita.pxl.util :refer [log]]
            [vickaita.pxl.view :as view]
            [vickaita.pxl.image-node :refer [image-node]]
            [vickaita.pxl.app :as app]
            [vickaita.raster.core :as ras :refer [image-data]]
            [vickaita.raster.filters :as filt]))  

(def app-state (atom app/empty-app))

(defn- open-file-picker
  []
  (.click (first (dom/nodes (dom/by-class "file-picker-input"))))
  nil)

(defn load-image-from-file
  [file]
  (when file
    (let [img (js/Image.)]
      (set! (.-onload img) #(let [img-node (image-node (image-data img) nil {:id :root})]
                              (swap! app-state app/add-node img-node)
                              (swap! app-state app/set-current img-node)
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
                   (swap! app-state app/apply-tranform tool-id))))
  (evt/listen! (dom/by-id "graph") :click
               (fn [e]
                 (evt/prevent-default e)
                 (evt/stop-propagation e)
                 (let [element (evt/target e)]
                   (when (dom/has-class? element "image-node")
                     (when-let [node (get-in @app-state [:graph :nodes (dom/attr element :id)])]
                       (swap! app-state app/set-current node))))))
  (evt/listen! (dom/by-id "control") :change
               (fn [e]
                 (evt/prevent-default e)
                 (evt/stop-propagation e)
                 (let [params (serialize-form (evt/current-target e))]
                   (swap! app-state app/update-transform-parameters params))))
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
