(ns justapp.layout
  (:require [net.cgrand.enlive-html :as html]
            [ring.util.response :refer [response response?]]
            [justapp.auth :as auth]))

(html/defsnippet menu-authenticated
  "layout.html" [:#menu-authenticated] [user]
  [:#user-profile] (html/content (auth/user-title user)))

(html/defsnippet menu-anonymous
  "layout.html" [:#menu-anonymous] [])

(defn- menu
  [req]
  (if-let [user (:user req)]
    (menu-authenticated user)
    (menu-anonymous)))

(html/deftemplate layout* "layout.html"
  [req main]
  [:#menu] (html/content (menu req))
  [:#flash] (html/content (:flash req))
  [:#main] (html/content (if (string? main) (html/html-snippet main) main))
  [:script] (fn [el] (update-in el [:attrs :src] #(str "/" %)))
  [:link] (fn [el] (update-in el [:attrs :href] #(str "/" %))))

(defn- update-response-after-flash
  "Force session value to clean up flash."
  [resp req]
  (if (:session resp)
    resp
    (if (:flash req)
      (assoc resp :session req)
      resp)))

(defn layout
  [req resp]
  (if (response? resp)
    (assoc resp :body (layout* req (:body resp)))
    (layout* req resp)))

(defn wrap-layout
  [handler]
  (fn [req]
    (layout req (handler req))))
