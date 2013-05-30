(ns vickaita.pxl.core
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :refer [resources not-found]]
            [ring.server.standalone :refer [serve]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]
            [vickaita.pxl.view :as view]))

(defroutes pxl-routes
  (GET "/" [] (view/index-page))
  (resources "/")
  (not-found {:status 404 :body "Not Found"}))

(def app (handler/site pxl-routes))

(defn go!
  []
  (serve #'app {:open-browser? false :auto-reload? true}))
