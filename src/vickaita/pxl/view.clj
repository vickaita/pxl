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
      [:input#file-picker-input {:type "file"}]
      [:select#tools
       [:option "-- Tools --"]]
      [:table
       [:thead [:tr [:th "Parent"] [:th "Data"]]]
       [:tfoot]
       [:tbody#graph]]]
     [:canvas#main-canvas ""]]))
