(ns vickaita.pxl.render
  (:require [goog.dom :as dom]
            [vickaita.raster.core :refer [put-image]]))

(defn main-canvas
  [cnv img]
  (if (string? img)
    (let [i (js/Image.)]
      (set! (.-onload i) #(put-image cnv i))
      (set! (.-src i) img))
    (put-image cnv img)))
