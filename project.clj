(defproject raster "0.1.0"
  :description "A library for working with ImageData in ClojureScript."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1806"]
                 [com.cemerick/clojurescript.test "0.0.3"]
                 [clj-aws-s3 "0.3.3"]
                 [ring "1.1.8"]
                 [ring-server "0.2.8"]
                 [compojure "1.1.5"]
                 [liberator "0.8.0"]
                 [com.novemberain/monger "1.4.1"]
                 [hiccup "1.0.1"]
                 [domina "1.0.2-SNAPSHOT"]
                 ;[prismatic/dommy "0.1.1"]
                 [shoreleave "0.3.0"]
                 [shoreleave/shoreleave-remote-ring "0.3.0"]]
  :plugins  [[lein-cljsbuild "0.3.2"]
             [lein-ring "0.8.2"]]
  :source-paths ["src/clj"]
  :cljsbuild {:builds
              [{:source-paths ["src/cljs"]
                :id "dev"
                :compiler {:pretty-print true
                           :output-to "resources/public/js/main.js"
                           ;:source-map "resources/public/js/main.map"  
                           :optimizations :whitespace}}
               {:source-paths ["src/cljs"]
                :id "prod"
                :compiler {:pretty-print false
                           :output-to "resources/public/js/main.js"
                           :optimizations :advanced}}]
              :repl-listen-port 9201}
  :ring {:handler vickaita.pxl.core/app})
