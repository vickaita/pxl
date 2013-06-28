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
     [:div.ui
      [:header.logo [:h1 "pxl"]]
      [:div.file-picker-wrap.hidden
       [:input.file-picker-input {:type "file" :accept "image/*"}]]  
      [:ul.tools]
      [:form#control]]   
     [:div#graph]]))
