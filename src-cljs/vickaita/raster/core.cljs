(ns vickaita.raster.core
  (:require [vickaita.raster.geometry :as geo]
            [goog.dom :as dom]))

;; Provides a protocol for accessing dimensions and pixel data of an image.
(defprotocol ImageData
  (width [this] "Returns the width of the ImageData.")
  (height [this] "Returns the height of the ImageData.")
  (data [this] "Returns the data of the ImageData."))

;; Provides a protocol for accessing ImageData of various HTML Elements as well
;; as some objects.
(defprotocol Imageable
  (-image-data [this] "Returns a js/ImageData object."))

;; A protocol for accessing the color channels from a pixel.
(defprotocol Pixel
  (red [pixel] "The red component of the pixel.")
  (green [pixel] "The green component of the pixel.")
  (blue [pixel] "The blue component of the pixel.")
  (alpha [pixel] "The alpha component of the pixel."))

(defprotocol Renderable
  (render [this target] "Paints this on the canvas."))

;; Constants

(def empty-pixel [0 0 0 0])

;; Utility functions

(defn pixel-array
  "Convert data into a Uint8ClampedArray."
  [data]
  (cond
    (= js/Uint8ClampedArray (type data)) data
    (coll? data) (if (map? (first data))
                  (js/Uint8ClampedArray. (into-array (flatten (map vals data))))
                  (js/Uint8ClampedArray. (into-array (flatten data))))))

(defn make-canvas
  "Create a canvas HTML tag. If width and height are provided then they will be
  set."
  ([] (dom/createElement "canvas"))
  ([w h]
   (let [cnv (dom/createElement "canvas")]
     (set! (.-width cnv) w)
     (set! (.-height cnv) h)
     cnv)))

(defn get-context
  "Gets a js/CanvasRenderingContext2D, either from a provided canvas or from a
  new canvas."
  ([] (get-context (make-canvas)))
  ([canvas] (.getContext canvas "2d"))
  ([w h] (get-context (make-canvas w h))))

(defn image-data
  "Returns a js/ImageData object from the provided object. If width and height
  are provided then a blank ImageData will be returned with the corresponding
  dimensions."
  ([i] (-image-data i))
  ([w h] (-image-data (make-canvas w h)))
  ([w h d] (-image-data {:width w :height h :data d})))

(defn put-image
  "Draws an image-data onto a canvas."
  ([cnv img]
   (let [i (image-data img)]
     (.putImageData (get-context cnv) i 0 0)))
  ([cnv img sx sy sw sh dx dy dw dh]
   (.putImageData (get-context cnv)
                  (image-data img)
                  sx sy sw sh dx dy dw dh)))

(defn drawable?
  [i]
  (and (< 0 (width i))
       (< 0 (height i))
       (< 0 (alength (data i)))))

;; Protocol Implementations

(extend-protocol ImageData
  js/ImageData
  (width [this] (.-width this))
  (height [this] (.-height this))
  (data [this] (.-data this))

  js/Image
  (width [this] (.-width this))
  (height [this] (.-height this))
  (data [this]
    (let [w (width this)
          h (height this)
          ctx (get-context w h)]
      (.drawImage ctx this 0 0 w h)
      (.-data (.getImageData ctx 0 0 w h))))

  ObjMap
  (width [this] (get this :width 0))
  (height [this] (get this :height 0))
  (data [this] (get this :data (js/Uint8ClampedArray.)))

  PersistentHashMap
  (width [this] (get this :width 0))
  (height [this] (get this :height 0))
  (data [this] (get this :data (js/Uint8ClampedArray.)))

  PersistentArrayMap
  (width [this] (get this :width 0))
  (height [this] (get this :height 0))
  (data [this] (get this :data (js/Uint8ClampedArray.)))

  nil
  (width [_] 0)
  (height [_] 0)
  (data [_] (js/Uint8ClampedArray.))
  )

(extend-protocol Imageable
  js/ImageData
  (-image-data [this] this)

  js/Image
  (-image-data [img]
    (let [w (.-width img)
          h (.-height img)
          ctx (get-context w h)]
      (.drawImage ctx img 0 0 w h)
      (.getImageData ctx 0 0 w h)))

  js/HTMLImageElement
  (-image-data [img]
    (let [w (.-width img)
          h (.-height img)
          ctx (get-context w h)]
      (.drawImage ctx img 0 0 w h)
      (.getImageData ctx 0 0 w h)))

  js/HTMLCanvasElement
  (-image-data [canvas]
    (image-data (.getContext canvas "2d")))

  js/CanvasRenderingContext2D
  (-image-data [ctx]
    (let [canvas (.-canvas ctx)
          w (.-width canvas)
          h (.-height canvas)]
      (.getImageData ctx 0 0 w h)))

  ObjMap
  (-image-data [{:keys [width height data]}]
    (when (and (> width 0) (> height 0) data)
      (let [blank (image-data width height)]
        (.set (.-data blank) (pixel-array data))
        blank)))

  PersistentHashMap
  (-image-data [{:keys [width height data]}]
    (when (and (> width 0) (> height 0) data)
      (let [blank (image-data width height)]
        (.set (.-data blank) (pixel-array data))
        blank)))

  PersistentArrayMap
  (-image-data [{:keys [width height data]}]
    (when (and (> width 0) (> height 0) data)
      (let [blank (image-data width height)]
        (.set (.-data blank) (pixel-array data))
        blank)))

  nil
  (-image-data [_] nil #_(-image-data {:width 0 :height 0 :data []}))
  )

(extend-protocol Pixel
  js/Array
  js/Uint8ClampedArray
  (red [a] (aget a 0))
  (green [a] (aget a 1))
  (blue [a] (aget a 2))
  (alpha [a] (aget a 3))

  PersistentVector
  (red [a] (nth a 0))
  (green [a] (nth a 1))
  (blue [a] (nth a 2))
  (alpha [a] (nth a 3)))

;; ImageDataSeq
;; Allows seq functions to be called on an ImageData.

(deftype ImageDataSeq [w h arr i]

  ;Object
  ;(toString [this]
  ;  (pr-str this))

  ;IPrintWithWriter
  ;(-pr-writer [coll writer opts]
  ;  (pr-sequential-writer writer pr-writer "(" " " ")" opts coll))

  Pixel
  (red [_] (aget arr (* 4 i)))
  (green [_] (aget arr (+ 1 (* 4 i))))
  (blue [_] (aget arr (+ 2 (* 4 i))))
  (alpha [_] (aget arr (+ 3 (* 4 i))))

  ImageData
  (width [_] w)
  (height [_] h)
  (data [_] arr)
  
  ISeqable
  (-seq [this] this)

  ASeq
  ISeq
  (-first [_] (let [offset (* 4 i)]
                [(aget arr offset)
                 (aget arr (+ 1 offset))
                 (aget arr (+ 2 offset))
                 (aget arr (+ 3 offset))]))
  (-rest [_] (if (< (* (inc i) 4) (alength arr))
                 (ImageDataSeq. w h arr (inc i))
                 (list)))

  INext
  (-next [_] (if (< (* (inc i) 4) (alength arr))
                 (ImageDataSeq. w h arr (inc i))
                 nil))

  ICounted
  (-count [_] (- (/ (alength arr) 4) i)) 

  IIndexed
  (-nth [coll n]
    (-nth coll n empty-pixel))
  (-nth [coll n not-found]
    (let [off (* 4 (+ n i))]
      (if (< off (alength arr))
        [(aget arr off)
         (aget arr (+ 1 off))
         (aget arr (+ 2 off))
         (aget arr (+ 3 off))]
        not-found)))

  ISequential
  IEquiv
  (-equiv [coll other] (equiv-sequential coll other))

  IEmptyableCollection
  (-empty [coll] cljs.core.List/EMPTY)

  IReduce
  (-reduce [coll f]
    (if (counted? arr)
      (ci-reduce arr f (aget arr i) (inc i))
      (ci-reduce coll f (aget arr i) 0)))
  (-reduce [coll f start]
    (if (counted? arr)
      (ci-reduce arr f start i)
      (ci-reduce coll f start 0)))

  IHash
  (-hash [coll] (hash-coll coll))

  IReversible
  (-rseq [coll]
    (let [c (-count coll)]
      (if (pos? c)
        (RSeq. coll (dec c) nil)
        ()))))

;; ImageData
;; Extend the native ImageData type with ClojureScript protocols so that it can
;; be operated on with standard collection functions.
(extend-type js/ImageData

  ICounted
  (-count [coll] (* (.-width coll) (.-height coll)))

  IIndexed
  (-nth [coll n]
    (-nth coll n empty-pixel))
  (-nth [coll n not-found]
    (let [pix (.-data coll)
          offset (* 4 n)]
      (if (and (>= n 0) (< n (count coll)))
        [(aget pix offset)        ; red
         (aget pix (+ 1 offset))  ; green
         (aget pix (+ 2 offset))  ; blue
         (aget pix (+ 3 offset))] ; alpha
        not-found)))

  ILookup
  (-lookup [coll k]
    (-lookup coll k empty-pixel))
  (-lookup [coll [x y] not-found]
    (if (and (>= x 0) (>= y 0)
             (< x (.-width coll)) (< y (.-height coll)))
      (-nth coll (+ x (* y (.-width coll))) not-found)
      not-found))

  IAssociative
  (-contains-key? [coll [x y]]
    (and (>= x 0)
         (>= y 0)
         (< x (.-width coll))
         (< y (.-height coll))))

  IFn
  (-invoke
    ([coll k]
     (-lookup coll k))
    ([coll k not-found]
     (-lookup coll k not-found)))

  ISeqable
  (-seq [coll]
    (ImageDataSeq. (.-width coll) (.-height coll) (.-data coll) 0))

)

(defn convolve
  "Apply a convolution matrix to an image.

  The matrix should be a square and it should have an odd route (e.g. 9 and 25
  are ok, but 16 and 36 are not). The cells of the matrix can be composed of
  integers (negative or positive) or of a 4 element vector with each component
  of the vector representing one of the red, green, blue, and alpha channels --
  in that order. The center cell of the matrix corresponds to the current
  pixel and the other cells represent the surrounding pixels."
  [matrix divisor offset src-img]
  (let [w (width src-img)
        h (height src-img)
        sdata (data src-img)
        dimg (image-data w h)
        ddata (data dimg)
        ct (geo/convolution-table matrix)
        ct-count (count ct)]
    (dotimes [dy h]
      (dotimes [dx w]
        (let [dest (* 4 (+ dx (* w dy)))]
          (loop [p 0 r-acc 0 g-acc 0 b-acc 0 a-acc 0]
            #_(when (= dy 5) (.log js/console p r-acc))
            (if (< p ct-count)
              (let [sx (+ dx (aget ct p))
                    sy (+ dy (aget ct (inc p)))]
                ; For some reason ClojureScript insists on creating an anon fn
                ; for the `and` block so inline the js for performance and
                ; ^boolean type hint to prevent call to cljs.core.truth_.
                (when ^boolean (js* "0 <= ~{} && ~{} < ~{} && 0 <= ~{} && ~{} < ~{}"
                                           sx     sx    w           sy     sy    h)
                  (let [src (* 4 (+ sx (* w sy)))]
                    (recur (+ p 6)
                           (+ r-acc (* (aget ct (+ 2 p)) (aget sdata (+ 0 src))))
                           (+ g-acc (* (aget ct (+ 3 p)) (aget sdata (+ 1 src))))
                           (+ g-acc (* (aget ct (+ 4 p)) (aget sdata (+ 2 src))))
                           (+ a-acc (* (aget ct (+ 5 p)) (aget sdata (+ 3 src))))))))
              (do (aset ddata (+ 0 dest) (+ offset (/ r-acc divisor)))
                  (aset ddata (+ 1 dest) (+ offset (/ g-acc divisor)))
                  (aset ddata (+ 2 dest) (+ offset (/ b-acc divisor)))
                  (aset ddata (+ 3 dest) (+ offset (/ a-acc divisor)))))))))
    (.log js/console dimg)
    dimg))

(defn crop
  [img x y w h]
  (let [cnv (make-canvas w h)]
    (put-image cnv img
               x y (+ x w) (+ y h)
               0 0      w       h)
    (image-data cnv)))

(defn scale
  [img w h]
  ;; TODO try to get rid of these null checks with polymorphism
  (if (and (not (nil? img)) (> 0 w) (> 0 h))
    (let [cnv (make-canvas w h)]
      (put-image cnv img 0 0 (width img) (height img) 0 0 w h)
      (image-data cnv))
    img))

(defn image-data->url
  [img]
  ;; TODO try to get rid of these null checks with polymorphism
  (if (nil? img)
    "data:image/png;base64,"
    (let [can (make-canvas (width img) (height img))]
      (put-image can img)
      (.toDataURL can))))
