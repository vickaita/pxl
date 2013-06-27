(ns vickaita.raster.macros)

(defn- vec4?
  "Predicate to check if expr is a vector of four elements."
  [expr]
  (and (vector? expr)
       (= 4 (count expr))))

(defmacro dopix
  [dst-data region row-size bindings body]
  (let [form (first bindings)
        src-data (second bindings)
        has-let (and (> (count bindings) 2) (= :let (nth bindings 2)))
        let-bindings (if has-let (nth bindings 3) [])]
    `(let [[x1# y1# x2# y2#] ~region]
       (loop [row# y1#]
         (loop [col# x1#]
           (let [row-offset# (* row# ~row-size 4)
                 r# (+ row-offset# (* col# 4))
                 g# (+ r# 1)
                 b# (+ r# 2)
                 a# (+ r# 3)]
             (let [~(nth form 0) (aget ~src-data r#)
                   ~(nth form 1) (aget ~src-data g#)
                   ~(nth form 2) (aget ~src-data b#)
                   ~(nth form 3) (aget ~src-data a#)
                   ~@let-bindings]
               (aset ~dst-data r# ~(nth body 0))
               (aset ~dst-data g# ~(nth body 1))
               (aset ~dst-data b# ~(nth body 2))
               (aset ~dst-data a# ~(nth body 3)))
             (when (< col# x2#) (recur (inc col#)))))
         (when (< row# y2#) (recur (inc row#))))
       {:data ~dst-data})))

(defmacro dopixels
  [bindings body]
  (let [form (first bindings)
        src-img (second bindings)
        has-let (and (> (count bindings) 2) (= :let (nth bindings 2)))
        let-bindings (if has-let (nth bindings 3) [])]
    `(let [w# (vickaita.raster.core/width ~src-img)
           h# (vickaita.raster.core/height ~src-img)
           src-data# (vickaita.raster.core/data ~src-img)
           dst-img# (vickaita.raster.core/image-data w# h#)
           dst-data# (vickaita.raster.core/data dst-img#)
           n# (alength dst-data#)]
       (loop [r# 0 g# 1 b# 2 a# 3]
         (let [~(nth form 0) (aget src-data# r#)
               ~(nth form 1) (aget src-data# g#)
               ~(nth form 2) (aget src-data# b#)
               ~(nth form 3) (aget src-data# a#)
               ~@let-bindings]
           (aset dst-data# r# ~(nth body 0))
           (aset dst-data# g# ~(nth body 1))
           (aset dst-data# b# ~(nth body 2))
           (aset dst-data# a# ~(nth body 3)))
         (when (> n# a#)
           (recur (+ 4 r#) (+ 4 g#) (+ 4 b#) (+ 4 a#))))
       dst-img#)))
