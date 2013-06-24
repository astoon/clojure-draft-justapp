(ns justapp.handlers
  (:require [monger.collection :as mc]
            [net.cgrand.enlive-html :as html]
            [ring.util.response]
            [justapp.auth :as auth]
            [justapp.cfg :refer [config]]))

;; Landing page

(html/defsnippet landing-page-template
  "layout.html" [:article] [])

(defn landing-page
  [request]
  (page request (landing-page-template)))

;; Sign Up

(html/defsnippet signup-form-template
  "layout.html" [:#signup-form] [])

(defn signup-form
  [request]
  (page request (signup-form-template)))

(defn signup-post
  [request]
  (let [email (:email (:params request))]
    (if (and (not (auth/find-user email))
             (not (auth/signup-exists? email)))
      (do (auth/signup-start email)
          (-> (ring.util.response/redirect "/")
              (assoc :flash "We've sent you a confirmation code!
                           Please check your email.")))
      (-> (ring.util.response/redirect "/signup")
          (assoc :flash "This address is already used.")))))

(html/defsnippet signup-confirm-template
  "layout.html" [:#signup-confirm] [email code]
  [:#hidden-email] (html/set-attr :value email)
  [:#hidden-code] (html/set-attr :value code))

(defn signup-confirm
  [{:keys [params] :as request}]
  (case (auth/signup-end (:email params)
                         (:code params)
                         (:password params))
      :success (assoc (ring.util.response/redirect "/") :flash "Your account has been created.")
      :no-password (page request
                         (signup-confirm-template (:email params)
                                                  (:code params)))
      :wrong-code (ring.util.response/redirect "/")
      :wrong-email (ring.util.response/redirect "/")))

;; Login

(html/defsnippet login-form-template
  "layout.html" [:#login-form] [])

(defn login-form
  [request]
  (page request (login-form-template)))

(html/deftemplate profile-form-template "profile.html"
  [firstname lastname]
  [:#profile-firstname] (html/set-attr :value firstname)
  [:#profile-lastname] (html/set-attr :value lastname))

;; User profile

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
