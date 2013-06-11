(ns vickaita.pxl.render
  (:require [domina :as dom]
            [domina.events :as evt]
            [vickaita.raster.core :as ras :refer [make-canvas width height put-image]]
            [clojure.walk :refer [walk]]))

(defn draw-tools!
  "Draw the tools in the UI. Clears out the tool list and iteratively redraws
  all tools."
  [tools]
  (let [tool-list (dom/by-class "tools")]
    (dom/destroy-children! tool-list)
    (doseq [[id tool] tools]
      (let [li (.createElement js/document "li")
            a (.createElement js/document "a")]
        (dom/set-attrs! li {:class "tool"})
        (dom/set-attrs! a {:class "tool-link" :id id :href "#"})
        (dom/set-text! a (:text tool))
        (dom/append! li a)
        (dom/append! tool-list li)))))

(defn draw-node!
  [element node is-current]
  (when node
    (if (= :root (:id node))
      nil
      (let [canvas (make-canvas (width node) (height node))]
        (put-image canvas node)
        (dom/set-attrs! canvas {:id (:id node)
                                :class (str "image-node" (when is-current " current"))})
        (dom/append! element canvas)))))

(defn draw-heirarchy!
  [current index element node children]
  (when node
    (let [div (.createElement js/document "div")]
      (dom/set-attrs! div {:class "graph"
                           :id (str "graph-of-" (if (keyword? node)
                                                     (name node)
                                                     (:id node)))})
      (draw-node! div node (= (:id node) (:id current)))
      (dom/prepend! element div)   
      (doseq [child children]
        (draw-heirarchy! current index div child (get index (:id child)))))))

(defn draw-app!
  [app]
  (let [element (dom/by-id "graph")
        current (:workspace app)
        nodes (get-in app [:graph :nodes])
        index (group-by :parent (vals nodes))]
    (dom/destroy-children! element)
    (draw-heirarchy! current index element {:id :root} (get index :root))))

#_(defn draw-graph!
  [graph]
  (let [element (dom/by-id "graph")
        heads (:heads graph)
        current (:current graph)
        nodes (:nodes graph)]
    (dom/destroy-children! element)
    (loop [remaining (apply dissoc nodes (map :id heads))
           tier heads]
      (when (not (empty? tier))
        (let [row (.createElement js/document "div")
              next-heads (set (remove nil? (map #(get remaining (:parent %)) tier)))  ]
          (dom/set-attr! row :class (str "row" (when (contains? tier current) " current")))
          (doseq [node tier] (when node (draw-node! row node (= node current))))
          (dom/append! element row)
          (recur (apply dissoc remaining (map :id next-heads))
                 next-heads))))))
