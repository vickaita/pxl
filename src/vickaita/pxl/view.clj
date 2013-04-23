(ns vickaita.pxl.view
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]))

(defn index-page
  []
  (html5
    [:head
     [:title "pxl"]
     (include-css "/css/main.css")
     (include-js "/js/main.js")]
    [:body
     [:div#ui
      [:header#logo [:h1 "pxl"]]
      [:hr]
      [:div#image-sources
       [:div#file-picker
        [:input#file-picker-input {:type "file"}]]
       [:ul#source-list]]]
     [:layers
      [:canvas.layer#main-canvas]]])) 
