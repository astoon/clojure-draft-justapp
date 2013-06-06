(ns justapp.layout
  (:require [net.cgrand.enlive-html :refer [deftemplate content]]))

(deftemplate layout "layout.html"
  [c]
  [:#main] (content c)
  [:script] (fn [el] (update-in el [:attrs :src] #(str "/" %)))
  [:link] (fn [el] (update-in el [:attrs :href] #(str "/" %))))
