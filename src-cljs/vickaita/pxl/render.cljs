(ns vickaita.pxl.render
  (:require [domina :as dom]
            [domina.events :as evt]
            [vickaita.raster.core :as ras :refer [width height put-image]]))

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
    (doseq [node nodes]
      (let [p (.createElement js/document "img")
            d (.createElement js/document "img")]
        (.setAttribute p "src" (thumb (:parent node)))
        (dom/append! graph
                     (dom/html-to-dom
                       (str "<tr>"
                            "<th>" (thumb (:parent node)) "</th>"
                            "<th>" (get node :data "no data") "</th>"
                            "</tr>")))))))

;; Event handlers

(defn handle-image-change
  [_ _ _ n]
  (when-let [new-image (:data n)]
    (let [w (width new-image)
          h (height new-image)]
      (resize-main-canvas w h)
      (redraw-main-canvas new-image))))

(defn handle-graph-change
  [_ _ _ nodes]
  (draw-graph nodes))

