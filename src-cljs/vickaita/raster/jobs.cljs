(ns vickaita.raster.jobs)

(def ^:dynamic *tile-size* 128)

(defn process-job
  [operation src-img]
  (let [w (vickaita.raster.core/width src-img)
        h (vickaita.raster.core/height src-img)
        src-data (vickaita.raster.core/data src-img)
        dst-img (vickaita.raster.core/image-data w h)
        dst-data (vickaita.raster.core/data dst-img)
        n (dec (* 4 (count src-img)))]
    (dotimes [i (* w h)])
    ))

#_(transform-bits src
                dst
                [0 0 128 128]
                [[(- 255 r) (- 255 g) (- 255 b) a] [r g b a]])
