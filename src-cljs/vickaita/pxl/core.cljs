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

(def ^{:doc "Holds the image-data for the currently displayed image."}
  current-image (atom nil))

(def ^{:doc "The graph holding all mutations on the images."}
  image-graph (atom {}))

#_(def ^{:doc "The graph holding all mutations on the images."}
  image-graph (atom {:heads #{}
                     :nodes {}
                     :current nil}))

(def tool-list
  (sorted-map
    "Invert" filt/invert
    "Blur" filt/blur
    "Desaturate" filt/desaturate))

;; View

(defn monitor-models
  []
  (add-watch current-image :image-change render/handle-image-change)
  (add-watch image-graph :graph-change render/handle-graph-change))

;; Controller

(defn load-image-from-file
  [file]
  (when file
    (let [img (js/Image.)]
      (set! (.-onload img) #(let [node (image-node (image-data img))]
                              (swap! image-graph assoc (:id node) node)
                              (reset! current-image node)
                              (set! (.-onload img) nil)))
      (set! (.-src img) (js/URL.createObjectURL file)))))

(defn apply-tool
  [e]
  (when-let [op-name (.-value (evt/current-target e))]
    (when-let [operation (tool-list op-name)]
      (let [new-image (image-node (operation @current-image) @current-image)]
        (reset! current-image new-image)
        (swap! image-graph assoc (:id new-image) new-image)))))

(defn monitor-dom
  []
  (let [fp-input (dom/by-id "file-picker-input")]
    (evt/listen! fp-input "change"
                 #(load-image-from-file (aget fp-input "files" 0)))) 
  (evt/listen! (dom/by-id "tools") :change apply-tool) 
  #_(evt/listen! :keydown #(.log js/console "keydown"))
  #_(evt/listen! :keyup #(.log js/console "keyup")))

(defn- main
  []
  (repl/connect "http://localhost:9201/repl")
  (render/prepare-tools tool-list)
  (monitor-models)
  (monitor-dom))

;; Kickoff the main function once the page loads
(evt/listen! js/document "DOMContentLoaded" main)
