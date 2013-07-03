(ns justapp.layout
  (:require [net.cgrand.enlive-html :as html]
            [cemerick.friend :refer [current-authentication]]
            [justapp.auth :as auth]))

(html/defsnippet menu-authenticated
  "layout.html" [:#menu-authenticated] [user]
  [:#user-profile] (html/content (auth/user-title user)))

(html/defsnippet menu-anonymous
  "layout.html" [:#menu-anonymous] [])

(defn- menu
  [req]
  (if-let [user (current-authentication req)]
    (menu-authenticated user)
    (menu-anonymous)))

(html/deftemplate layout-template "layout.html"
  [req content]
  [:#menu] (html/content (menu req))
  [:#flash] (html/content (:flash req))
  [:#main] (html/content content)
  [:script] (fn [el] (update-in el [:attrs :src] #(str "/" %)))
  [:link] (fn [el] (update-in el [:attrs :href] #(str "/" %))))
