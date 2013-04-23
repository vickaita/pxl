(ns vickaita.pxl.core
  (:require [goog.dom :as dom]
            [goog.dom.ViewportSizeMonitor]
            [goog.events :as events]
            [clojure.browser.repl :as repl]
            [vickaita.pxl.app :as app]
            [vickaita.raster.core :refer [put-image image-data]]))

(def main-canvas (atom nil))

(def image-list (atom ()))

(def canvas-size (atom {:width 0 :height 0}))

(def image-offset (atom {:x 0 :y 0}))

(def images (atom {}))

(defn add-image
  [data-url])

(defn- redraw-canvas
  []
  (.log js/console "redraw")
  (when (first @image-list)
    (let [i (js/Image.)
          ctx (.getContext @main-canvas "2d")
          {sx :x sy :y} @image-offset
          dx 0 dy 0
          {dw :width dh :height} @canvas-size ]
      (set! (.-onload i) #(.putImageData ctx (image-data i) sx sy dx dy dw dh))
      (set! (.-src i) (first @image-list)))))

(defn- resize-canvas
  []
  (doto @main-canvas
    (.setAttribute "width" (:width @canvas-size))
    (.setAttribute "height" (:height @canvas-size))))

(defn monitor-image-list
  []
  (add-watch image-list :redraw-canvas redraw-canvas)
  (add-watch image-offset :image-offset redraw-canvas) 
  (add-watch canvas-size :resize-canvas #(do (resize-canvas)
                                             (redraw-canvas))))

(defn monitor-viewport-size
  []
  (let [vsm (goog.dom.ViewportSizeMonitor.)]
    (let [size (.getSize vsm)]
      (reset! canvas-size {:width (.-width size) :height (.-height size)}))
    (events/listen
      vsm goog.events.EventType.RESIZE
      #(let [size (.getSize vsm)]
         (reset! canvas-size {:width (.-width size) :height (.-height size)})))))

(defn monitor-load-image
  []
  (let [fp-input (dom/getElement "file-picker-input")]
    (events/listen
      fp-input "change"
      #(let [file (aget (.-files fp-input) 0) ]
         (swap! image-list conj (js/URL.createObjectURL file))))))

(defn monitor-keys
  []
  (events/listen js/document "keydown" #(.log js/console "keydown"))
  (events/listen js/document "keyup" #(.log js/console "keyup"))) 

(defn handle-move-canvas
  []
  (events/listen
    @main-canvas "mousedown"
    (fn [e]
      (.log js/console "mousedown")
      (let [x (.-clientX e)
            y (.-clientY e)
            mv-key (events/listen
                     @main-canvas "mousemove"
                     #(reset! image-offset {:x (- (.-clientX %) x)
                                            :y (- (.-clientY %) y)}))]
        (events/listenOnce
          @main-canvas "mouseup"
          #(events/unlistenByKey mv-key))))))

(defn- main
  []
  (repl/connect "http://localhost:9201/repl")
  (reset! main-canvas (dom/getElement "main-canvas"))
  (monitor-image-list)
  (monitor-viewport-size)
  (monitor-load-image)
  #_(monitor-keys)
  (handle-move-canvas))

;; Kickoff the main function once the page loads
(events/listen js/document "DOMContentLoaded" main)
