(ns vickaita.raster.filters-test
  (:require [cemerick.cljs.test :refer (test-ns)]
            [vickaita.raster.core :as c]
            [vickaita.raster.filters :as f]
            [goog.dom :as dom])
  (:require-macros [cemerick.cljs.test :refer (is deftest run-tests testing)]))  

(deftest duplicate-test
  (let [img (c/image-data {:width 2 :height 1 :data (range 8)})
        dup (f/duplicate img)]
    (testing "duplicate"
      (is (= 2 (c/width dup)))
      (is (= 1 (c/height dup))) 
      (is (= [0 1 2 3] (first dup)))
      (is (= [4 5 6 7] (first (rest dup))))
      )))

(deftest invert-test
  (let [img (c/image-data {:width 3 :height 2 :data (range 24)})
        inv (f/invert img)]
    (.log js/console inv)
    (testing "invert"
      (is (= 3 (c/width inv)))
      (is (= 2 (c/height inv)))
      (is (= 6 (count inv)))
      (is (= [255 254 253 3] (first inv))))
    ))

(test-ns 'vickaita.raster.filters-test)
