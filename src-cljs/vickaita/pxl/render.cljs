(ns vickaita.pxl.render
  (:require [domina :as dom]
            [domina.events :as evt]
            [vickaita.raster.core :as ras :refer [make-canvas width height put-image]]
            [clojure.walk :refer [walk]]))

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

(defn draw-tools
  "Draw the tools in the UI. Clears out the tool list and iteratively redraws
  all tools."
  [tool-map]
  (let [tool-list (dom/by-class "tools")]
    (dom/destroy-children! tool-list)
    (doseq [[id tool] tool-map]
      (let [li (.createElement js/document "li")
            a (.createElement js/document "a")]
        (dom/set-attrs! li {:class "tool"})
        (dom/set-attrs! a {:class "tool-link" :id id :href "#"})
        (dom/set-text! a (:text tool))
        (dom/append! li a)
        (dom/append! tool-list li)))))

(defn redraw-main-canvas
  "Draw the provided imate to the main canvas."
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

(defn draw-node
  [element node]
  (let [canvas (make-canvas (width node) (height node))]
    (put-image canvas node)
    (dom/append! element canvas)))

(defn draw-graph
  [graph]
  (let [element (dom/by-id "graph")
        heads (:heads graph)
        current (:current graph)
        nodes (:nodes graph)]
    (dom/destroy-children! element)
    (loop [tier heads]
      (when (not (empty? tier))
        (let [row (.createElement js/document "div")]
          (doseq [node tier] (draw-node row node))
          (dom/append! element row))
        (recur (set (remove nil? (map #(get nodes (:parent %)) tier))))))))

;; Event handlers

(defn handle-graph-change [_ _ _ n] (draw-graph n))
