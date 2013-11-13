(defproject raster "0.1.0"
  :description "A library for working with ImageData in ClojureScript."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2030"]
                 [org.clojure/core.async "0.1.256.0-1bf8cf-alpha"]
                 [com.cemerick/clojurescript.test "0.0.3"]
                 [org.clojure/clojure "1.5.1"]
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
  :cljsbuild {:repl-listen-port 9201 
              :builds
              [{:id "dev"
                :source-paths ["src/cljs"]
                :compiler {:output-to "main.js"
                           :output-dir "resources/public/js/"
                           :pretty-print true
                           :source-map true
                           :optimizations :none}}
               {:id "prod"
                :source-paths ["src/cljs"]
                :compiler {:pretty-print false
                           :output-to "main.js"
                           :output-dir "resources/public/js/"
                           :optimizations :advanced}}]}
  :ring {:handler vickaita.pxl.core/app})

