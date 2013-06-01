(ns vickaita.pxl.core
  (:require [domina :as dom]
            [domina.events :as evt]
            [goog.dom.ViewportSizeMonitor]
            [clojure.browser.repl :as repl]
            [vickaita.pxl.render :as render]
            [vickaita.pxl.image-node :refer [image-node]]
            [vickaita.raster.core :as ras :refer [image-data]]
            [vickaita.raster.filters :as filt]))  

(defn log [& msg] (.log js/console (apply str msg)))

;; Model

(def ^{:doc "Holds the image-data for the currently displayed image."}
  current-image (atom nil))

(def ^{:doc "The graph holding all mutations on the images."}
  image-graph (atom {}))

#_(def ^{:doc "The graph holding all mutations on the images."}
  image-graph (atom {:heads #{}
                     :nodes {}
                     :current nil}))

(defn- open-file-picker
  []
  (log "open file picker")
  (.click (first (dom/nodes (dom/by-class "file-picker-input"))))
  nil)

(defn load-image-from-file
  [file]
  (when file
    (let [img (js/Image.)]
      (set! (.-onload img) #(let [node (image-node (image-data img))]
                              (reset! current-image node)
                              (swap! image-graph assoc (:id node) node)
                              (set! (.-onload img) nil)))
      (set! (.-src img) (js/URL.createObjectURL file)))))

(def tool-map
  (sorted-map
    "LoadImage" {:text "Load an Image" :icon "" :fn (fn [img]
                                                      (log "open file picker event handler")
                                                      (open-file-picker))}
    "Invert" {:text "Invert" :icon "" :fn filt/invert}
    "Blur" {:text "Blur" :icon "" :fn filt/blur}
    "Desaturate" {:text "Desaturate" :icon "" :fn filt/desaturate}
    "Sobel" {:text "Sobel (broken)" :icon "" :fn filt/sobel}
    "Sharpen" {:text "Sharpen" :icon "" :fn filt/sharpen}))

;; View

(defn monitor-models
  []
  (add-watch current-image :image-change render/handle-image-change)
  (add-watch image-graph :graph-change render/handle-graph-change))

;; Controller

(defn apply-tool
  [tool-id]
  (log "apply tool")
  (when-let [operation (tool-map tool-id)]
    (log "a")
    (log operation)
    (when-let [image ((:fn operation) @current-image)]
      (log "b")
      (let [node (image-node image @current-image)]
        (log "c")
        (reset! current-image node)
        (swap! image-graph assoc (:id node) node)))))

(defn monitor-dom
  []
  (let [file-picker (first (dom/nodes (dom/by-class "file-picker-input")))]
    (evt/listen! file-picker :change
                 (fn [e]
                   (evt/prevent-default e)
                   (evt/stop-propagation e)
                   (log "change in file picker input")
                   (load-image-from-file (aget file-picker "files" 0))))) 
  (evt/listen! (dom/by-class "tools") :click
               (fn [e]
                 (evt/prevent-default e)
                 (evt/stop-propagation e)
                 (when-let [tool-id (dom/attr (evt/target e) :id)]
                   (apply-tool tool-id))))
  #_(evt/listen! :keydown #(log "keydown"))
  #_(evt/listen! :keyup #(log "keyup")))

(defn- main
  []
  (repl/connect "http://localhost:9201/repl")
  (render/prepare-tools tool-map)
  (monitor-models)
  (monitor-dom))

;; Kickoff the main function once the page loads
(evt/listen! js/document "DOMContentLoaded" main)
