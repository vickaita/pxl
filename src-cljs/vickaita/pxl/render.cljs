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

(defn draw-tools!
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

(defn draw-node!
  [element node is-current]
  (let [canvas (make-canvas (width node) (height node))]
    (put-image canvas node)
    (dom/set-attrs! canvas {:id (:id node)
                            :class (str "image-node" (when is-current " current"))})
    (dom/append! element canvas)))

(defn draw-graph!
  [graph]
  (let [element (dom/by-id "graph")
        heads (:heads graph)
        current (:current graph)
        nodes (:nodes graph)]
    (dom/destroy-children! element)
    (loop [tier heads]
      (when (not (empty? tier))
        (let [row (.createElement js/document "div")]
          (doseq [node tier] (draw-node! row node (= node current)))
          (dom/append! element row))
        (recur (set (remove nil? (map #(get nodes (:parent %)) tier))))))))
