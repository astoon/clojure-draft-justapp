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
  [req content]
  [:#menu] (html/content (menu req))
  [:#flash] (html/content (:flash req))
  [:#main] (html/content content)
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

(defn treat-response
  [resp]
  (if (response? resp)
    resp
    (response resp)))

(defn layout
  "Make final response with body wrapped into layout.
  Takes request and response arguments.
  Response argument can be one of:
  - string,
  - ring response map,
  - sequence produced by cgrand/enlive's html-snippet.
  "
  [req resp]
  (-> resp
      ;(treat-response resp)
      ;(update-response-after-flash req)
      (assoc :body (layout* req (:body resp)))))

(defn wrap-layout
  "Wrap responses into layout."
  [handler]
  (fn [req]
    (layout req (handler req))))
