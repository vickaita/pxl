(ns vickaita.pxl.image-node)

(defn image-node
  ([data] {:parent nil :data data})
  ([data parent] {:parent parent :data data}))

;; TODO
(defn persist-node
  [node]
  (.log js/console "saving node to server"))
