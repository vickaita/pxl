(ns vickaita.pxl.util)

(defn log [& msgs] (.log js/console (apply str msgs)))

(defn benchmark
  [f k]
  (fn [& args]
    (let [start (.now js/Date)
          result (apply f args)
          end (.now js/Date)
          delta (- end start)]
      (.log js/console (str "Method " k "took " delta "ms to complete."))
      result)))
