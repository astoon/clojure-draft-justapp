(ns justapp.page
  (:require [net.cgrand.enlive-html :refer [defsnippet set-attr]]
            [monger.collection :as mc]
            [ring.util.response :refer [redirect]]
            [justapp.layout :as layout]
            [justapp.auth :as auth]
            [justapp.tree :as tree]))

(defsnippet frontpage-content
  "frontpage.html" [:#content] [])

(defn frontpage []
  (layout/layout (frontpage-content)))

(defsnippet signup-confirm-content
  "signup_confirm.html"
  [:form]
  [email code]
  [:#hidden-email] (set-attr :value email)
  [:#hidden-code] (set-attr :value code))

(defn signup-confirm
  [{params :params}]
  (let [email (:email params)
        code (:code params)
        p1 (:password params)
        p2 (:confirm params)]
    (if-let [x (mc/find-one-as-map "signup" {:email email})]
      (if (= code (:code x))
        (if (and (not (nil? p1)) (= p1 p2))
          (do (mc/remove "signup" {:email email})
              (auth/create-authenticated-user email p1)
              (-> (redirect "/")
                  (assoc :flash "Your account has been created.")))
          (layout/layout (signup-confirm-content email code)))
        (redirect "/"))
      (redirect "/"))))
