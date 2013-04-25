(ns vickaita.pxl.render
  (:require [domina :as dom]
            [domina.events :as evt]
            [vickaita.raster.core :refer [put-image]]))

(defn prepare-tools
  [tool-list]
  (let [tool-select (dom/by-id "tools")]
    (doseq [tool tool-list]
      (dom/append!
        tool-select
        (dom/html-to-dom
          (str "<option value=\"" (key tool) "\">" (key tool) "</option>"))))))

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
