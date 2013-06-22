(ns vickaita.pxl.core
  (:require [domina :as dom]
            [domina.events :as evt]
            [goog.dom.ViewportSizeMonitor]
            [clojure.browser.repl :as repl]
            [vickaita.pxl.util :refer [log]]
            [vickaita.pxl.view :as view]
            [vickaita.pxl.image-node :refer [image-node]]
            [vickaita.pxl.app :as app]
            [vickaita.raster.core :as ras :refer [image-data width height data]]
            [vickaita.raster.filters :as filt]))  

(def app-state (atom app/empty-app))

(defn- load-image
  [_ write-image-data]
  (let [file-picker (first (dom/nodes (dom/by-class "file-picker-input")))
        file-picker-wrap (first (dom/nodes (dom/by-class "file-picker-wrap")))]
    (evt/listen-once!
      file-picker :change
      (fn [e]
        (evt/prevent-default e)
        (evt/stop-propagation e)
        (dom/add-class! file-picker-wrap "hidden") 
        (when-let [file (aget file-picker "files" 0)]    
          (let [img (js/Image.)]
            (set! (.-onload img)
                  #(do (write-image-data (image-data img))
                       (set! (.-onload img) nil)))
            (set! (.-src img) (js/URL.createObjectURL file))))))
    (dom/remove-class! file-picker-wrap "hidden"))
  ;; TODO: do something better here
  (image-data {:width 0 :height 0 :data nil}))

#_(log (get-in @app-state [:graph :nodes]))

(defn serialize-form
  [form]
  (let [inputs (.getElementsByTagName form "input")]
    (vec (for [input inputs] (.-value input)))))

(def tool-map
  {"t1" {:id "t1" :text "Load an Image"  :transform load-image :control nil}
   "t2" {:id "t2" :text "Invert"         :transform filt/invert      :control nil}
   "t3" {:id "t3" :text "Blur"           :transform filt/blur        :control nil}
   "t4" {:id "t4" :text "Desaturate"     :transform filt/desaturate  :control nil}
   ;"t5" {:id "t5" :text "Sobel (broken)" :transform filt/sobel       :control nil}
   "t6" {:id "t6" :text "Sharpen"        :transform filt/sharpen     :control nil}
   "t7" {:id "t7" :text "Brighten"       :transform filt/brighten    :control [{:type "range" :min 0 :max 255 :value 0}]}})

(defn monitor-app
  []
  (add-watch app-state :app-change view/draw-app!))

(defn monitor-dom
  []
  (evt/listen! (dom/by-class "tools") :click
               (fn [e]
                 (evt/prevent-default e)
                 (evt/stop-propagation e)
                 (when-let [tool-id (dom/attr (evt/target e) :id)]
                   (swap! app-state app/transform tool-id))))
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

(defn monitor-jobs
  []
  (let [[job & jobs] (:render-jobs @app-state)]
    (if job
      (do (swap! app-state assoc :render-jobs jobs)
          ((:function job)
           (partial app/get-node @app-state)
           (fn [node]
             (swap! app-state app/set-node node)
             (js/setTimeout monitor-jobs 0))))
      (js/setTimeout monitor-jobs 100))))

(defn- main
  []
  (repl/connect "http://localhost:9201/repl")
  (monitor-app)
  (monitor-dom)
  (monitor-jobs)
  (swap! app-state assoc :tools tool-map))

;; Kickoff the main function once the page loads
(evt/listen! js/document "DOMContentLoaded" main)
