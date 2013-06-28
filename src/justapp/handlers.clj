(ns justapp.handlers
  (:require [net.cgrand.enlive-html :as html]
            [cemerick.friend :as friend]
            [ring.util.response :refer [redirect]]
            [justapp.auth :as auth]))

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
  "layout.html" [:#login-form] [req]
  [[:input (html/attr= :name "username")]] (html/set-attr :value (:username (:params req))))

;; User profile

(html/defsnippet profile-form-template
  "layout.html" [:#profile-form] [firstname lastname]
  [[:input (html/attr= :name "firstname")]] (html/set-attr :value firstname)
  [[:input (html/attr= :name "lastname")]] (html/set-attr :value lastname))

(defn profile-form
  [req]
  (let [user (friend/current-authentication req)]
    (profile-form-template (:firstname user) (:lastname user))))

(defn profile-post
  [{:keys [params] :as req}]
  (let [firstname (or (:firstname params) "")
        lastname (or (:lastname params) "")
        user (friend/current-authentication req)]
    (auth/update-user-profile (:_id user) firstname lastname)

    ; update user info in session
    (let [user (assoc user :firstname firstname :lastname lastname)
          session (:session (friend/merge-authentication req user))]
      (-> (redirect "/")
          (assoc :session session)))))
