(ns vickaita.pxl.render
  (:require [domina :as dom]
            [domina.events :as evt]
            [vickaita.raster.core :as ras :refer [width height put-image]]))

(defn benchmark
  [f k]
  (fn [& args]
    (let [start (.now js/Date)
          result (apply f args)
          end (.now js/Date)
          delta (- end start)]
      (.log js/console (str "Method " k "took " delta "ms to complete."))
      result)))

;; Initialization

(defn prepare-tools
  [tool-list]
  (let [tool-select (dom/by-id "tools")]
    (doseq [tool tool-list]
      (dom/append!
        tool-select
        (dom/html-to-dom
          (str "<option value=\"" (key tool) "\">" (key tool) "</option>"))))))

;; Rendering

(defn redraw-main-canvas
  [img]
  (put-image (dom/by-id "main-canvas") img))

(defn resize-main-canvas
  "Resize the canvas and recenter it as well."
  [w h]
  (when-let [cnv (dom/by-id "main-canvas")]
    (doto cnv
      (.setAttribute "width" w)
      (.setAttribute "height" h)
      (.setAttribute "style" (str "margin-top:"
                                  (max 0 (- (/ (.-innerHeight js/window) 2)
                                            (/ h 2)))
                                  "px;"
                                  "margin-left:"
                                  (max 0 (- (/ (.-innerWidth js/window) 2)
                                            (/ w 2)))
                                  "px")))))

(defn thumb
  [img]
  (dom/set-attrs! (.createElement js/document "img")
                  {:src (ras/image-data->url (ras/scale img 40 40))
                   :width 40
                   :height 40}))

(defn draw-graph
  [nodes]
  (let [graph (dom/by-id "graph")]
    (dom/destroy-children! graph)
    (doseq [node (vals nodes)]
      (let [p (thumb (get nodes (:parent node)))
            n (thumb node)
            d (.createElement js/document "div")]
      (dom/append! d [p n])
      (dom/append! graph d)))))

;; Event handlers

(defn handle-image-change
  [_ _ _ node]
  (when node
    (resize-main-canvas (width node) (height node))
    (redraw-main-canvas node)))

(defn handle-graph-change
  [_ _ _ nodes]
  (draw-graph nodes))

