(ns vickaita.pxl.view
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
    (when (not= :root (:id node))
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

(defn draw-control!
  [control]
  (let [element (dom/by-id "control")]
    (dom/destroy-children! element)
    (doseq [attrs control]
      (let [input (.createElement js/document "input")]
        (dom/set-attrs! input attrs)
        (dom/append! element input)))))

(defn draw-app!
  [_ _ old app]
  (let [different-in? (fn [k] (not= (get-in old k) (get-in app k)))]
    (when (different-in? [:tools]) (draw-tools! (:tools app)))
    (when (different-in? [:graph :nodes (:current app) :tool :control])
      (draw-control! (get-in app [:graph :nodes (:current app) :tool :control])))
    (when (or (different-in? [:graph]) (different-in? [:current]))
      (let [element (dom/by-id "graph")
            current (get-in app [:graph :nodes (:current app)])
            index (group-by :parent (vals (->> app :graph :nodes)))]
        (dom/destroy-children! element)
        (draw-heirarchy! current index element {:id :root} (get index :root))))))
