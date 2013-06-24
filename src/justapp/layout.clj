(ns justapp.layout
  (:require [net.cgrand.enlive-html :as html]
            [ring.util.response]
            [justapp.auth :as auth]))

(html/defsnippet menu-authenticated
  "layout.html" [:#menu-authenticated] [user]
  [:#user-profile] (html/content (auth/user-title user)))

(html/defsnippet menu-anonymous
  "layout.html" [:#menu-anonymous] [])

(defn- menu
  [request]
  (if-let [user (:user request)]
    (menu-authenticated user)
    (menu-anonymous)))

(html/deftemplate layout "layout.html"
  [request content]
  [:#menu] (html/content (menu request))
  [:#flash] (html/content (:flash request))
  [:#main] (html/content content)
  [:script] (fn [el] (update-in el [:attrs :src] #(str "/" %)))
  [:link] (fn [el] (update-in el [:attrs :href] #(str "/" %))))

(defn- update-response-after-flash
  "Force session value to clean up flash."
  [request response]
  (if (:session response)
    response
    (if (:flash request)
      (assoc response :session request)
      response)))

(defn treat-response
  [response]
  (cond
   (map? response) response
   (string? response) (ring.util.response/response response)))

(defn page
  "Make final response wrapping body into layout.
  If taken response is string, then wrap it into standard ring's
  response map."
  [request response]
  (-> (update-response-after-flash request response)
      (assoc :body (layout request (:body response)))))
