(ns vickaita.pxl.core
  (:require [domina :as dom]
            [domina.events :as evt]
            [goog.dom.ViewportSizeMonitor]
            [clojure.browser.repl :as repl]
            [vickaita.pxl.util :refer [log]]
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

;; ---

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
      (set! (.-onload img) #(let [img-node (image-node (image-data img))]
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
   "t7" {:id "t7" :text "Brighten"       :transform filt/brighten   :control {:type :form
                                                                              :inputs [{:type "range" :min 0 :max 255 :value 0}]}}})

;; Controller

(defn apply-tranform
  [tool-id params]
  (when-let [tool (get-in @app-state [:tools tool-id])]
    (when-let [image (apply (:transform tool) (conj params (get-current @app-state)))]
      (let [old-node (get-current @app-state)
            new-node (image-node image tool params old-node)]
        (swap! app-state #(-> %
                              (add-node new-node)
                              (set-current new-node)))))))

(defn update-transform-parameters
  [parameters]
  (let [old-node (get-current @app-state)
        new-node (assoc old-node :parameters parameters)]
    (swap! app-state set-current new-node)))

(defn serialize-form
  [form]
  (let [inputs (.getElementsByTagName form "input")]
    (for [input inputs] (.-value input))))

(defn monitor-app
  []
  (add-watch app-state :app-change (fn [_ _ _ n] (render/draw-app! n))))

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
                   (apply-tranform tool-id (serialize-form (dom/by-id "control"))))))
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
                 (update-transform-parameters (serialize-form (evt/target e)))))
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
