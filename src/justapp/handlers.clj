(ns justapp.handlers
  (:require [monger.collection :as mc]
            [net.cgrand.enlive-html :as html]
            [ring.util.response :refer [redirect]]
            [justapp.auth :as auth]
            [justapp.cfg :refer [config]]))

;; Landing page

(html/defsnippet landing-page
  "layout.html" [:article] [])

;; Sign Up

(html/defsnippet signup-form
  "layout.html" [:#signup-form] [])

(defn signup-post
  [email]
  (if (and (not (auth/find-user email))
           (not (auth/signup-exists? email)))
    (do (auth/signup-start email)
        (-> (redirect "/")
            (assoc :flash "We've sent you a confirmation code!
                           Please check your email.")))
    (-> (redirect "/signup")
        (assoc :flash "This address is already used."))))

(html/defsnippet signup-confirm-template
  "layout.html" [:#signup-confirm] [email code]
  [:#hidden-email] (html/set-attr :value email)
  [:#hidden-code] (html/set-attr :value code))

(defn signup-confirm
  [email code password]
  (case (auth/signup-end email code password)
      :success (assoc (redirect "/") :flash "Your account has been created.")
      :no-password (signup-confirm-template email code)
      :wrong-code (redirect "/")
      :wrong-email (redirect "/")))

;; Login

(html/defsnippet login-form
  "layout.html" [:#login-form] [])

;; User profile

(html/deftemplate profile-form-template "profile.html"
  [firstname lastname]
  [:#profile-firstname] (html/set-attr :value firstname)
  [:#profile-lastname] (html/set-attr :value lastname))

(defn profile-form
  [{user :user}]
  (apply str (profile-form-template (:firstname user)
                                    (:lastname user))))

(defn profile-post
  [{params :params user :user}]
  (mc/update-by-id "users"
                   (:_id user)
                   {"$set" {:firstname (:firstname params)
                            :lastname (:lastname params)}})
  {:status 204})
