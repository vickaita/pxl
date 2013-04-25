(ns vickaita.pxl.core
  (:require [domina :as dom]
            [domina.events :as evt]
            [goog.dom.ViewportSizeMonitor]
            [clojure.browser.repl :as repl]
            [vickaita.pxl.render :as render]
            [vickaita.pxl.image-node :refer [image-node]]
            [vickaita.raster.core :as ras]
            [vickaita.raster.filters :as filt]))  

;; Model

(def ^{:doc "Holds the image-data for the currently displayed image."}
  current-image (atom nil))

(def ^{:doc "The graph holding all mutations on the images."}
  image-graph (atom #{}))

(def tool-list
  (sorted-map
    "Invert" filt/invert
    "Blur" filt/blur
    "Desaturate" filt/desaturate))

;; View

(defn monitor-current-image
  []
  (add-watch current-image :on-change (fn [_ _ _ n]
                                        (let [w (ras/width n)
                                              h (ras/height n)]
                                          (render/resize-main-canvas w h)
                                          (render/redraw-main-canvas @current-image)))))

;; Controller

(defn load-image-from-file
  [file]
  (let [img (js/Image.)]
    (set! (.-onload img) #(let [node (image-node (ras/image-data img))]
                            (swap! image-graph conj node)
                            (reset! current-image (:data node))
                            (set! (.-onload img) nil)))
    (set! (.-src img) (js/URL.createObjectURL file))))

(defn monitor-loader
  []
  (let [fp-input (dom/by-id "file-picker-input")]
    (evt/listen! fp-input "change"
                    #_(load-image-from-file (aget (.-files fp-input) 0))
                    #(load-image-from-file (aget fp-input "files" 0)))))

(defn monitor-tools
  []
  (evt/listen! (dom/by-id "tools")
               :change (fn [e]
                         (when-let [op-name (.-value (evt/current-target e))]
                           (swap! current-image (tool-list op-name))))))

(defn monitor-keys
  []
  (evt/listen! :keydown #(.log js/console "keydown"))
  (evt/listen! :keyup #(.log js/console "keyup")))

(defn- main
  []
  (repl/connect "http://localhost:9201/repl")
  (render/prepare-tools tool-list)
  (monitor-current-image)
  (monitor-loader)
  (monitor-tools)
  #_(monitor-keys))

;; Kickoff the main function once the page loads
(evt/listen! js/document "DOMContentLoaded" main)
