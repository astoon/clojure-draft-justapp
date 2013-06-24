(ns justapp.handlers
  (:require [monger.collection :as mc]
            [net.cgrand.enlive-html :as html]
            [ring.util.response :refer [redirect]]
            [justapp.auth :as auth]
            [justapp.cfg :refer [config]]
            [justapp.layout :refer [page]]))

;; Landing page

(html/defsnippet landing-page-template
  "layout.html" [:article] [])

(defn landing-page
  [req]
  (page req (landing-page-template)))

;; Sign Up

(html/defsnippet signup-form-template
  "layout.html" [:#signup-form] [])

(defn signup-form
  [req]
  (page req (signup-form-template)))

(defn signup-post
  [req]
  (let [email (:email (:params req))]
    (if (and (not (auth/find-user email))
             (not (auth/signup-exists? email)))
      (do (auth/signup-start email)
          (-> (redirect "/")
              (assoc :flash "We've sent you a confirmation code!
                           Please check your email.")))
      (-> (redirect "/signup")
          (assoc :flash "This address is already used.")))))

(html/defsnippet signup-confirm-template
  "layout.html" [:#signup-confirm] [email code]
  [:#hidden-email] (html/set-attr :value email)
  [:#hidden-code] (html/set-attr :value code))

(defn signup-confirm
  [{:keys [params] :as req}]
  (case (auth/signup-end (:email params)
                         (:code params)
                         (:password params))
      :success (assoc (redirect "/") :flash "Your account has been created.")
      :no-password (page req
                         (signup-confirm-template (:email params)
                                                  (:code params)))
      :wrong-code (redirect "/")
      :wrong-email (redirect "/")))

;; Login

(html/defsnippet login-form-template
  "layout.html" [:#login-form] [])

(defn login-form
  [req]
  (page req (login-form-template)))

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
