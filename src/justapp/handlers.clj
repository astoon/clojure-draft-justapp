(ns justapp.handlers
  (:require [monger.collection :as mc]
            [net.cgrand.enlive-html :as html]
            [ring.util.response :refer [response redirect]]
            [justapp.auth :as auth]
            [justapp.cfg :refer [config]]))

;; Layout

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

;; Sign Up

(html/defsnippet signup-form-template
  "layout.html" [:#signup-form] [])

(defn signup-form
  [request]
  (layout request (signup-form-template)))

(defn signup-post
  [request]
  (let [email (:email (:params request))]
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
  [{params :params}]
  (let [email (:email params)
        code (:code params)]
    (case (auth/signup-end email code (:password params) (:confirm params))
      :success (-> (redirect "/")
                   (assoc :flash "Your account has been created."))
      :wrong-password (layout (signup-confirm-template email code))
      :wrong-code (redirect "/")
      :wrong-email (redirect "/"))))

;; Login

(html/deftemplate login-form-template
  "loginform.html" [])

(defn login-form []
  (apply str (login-form-template)))

(defn login-post
  [{params :params session :session}]
  (if-let [userid (auth/authenticate (:email params) (:password params))]
    (assoc-in (response {:success true}) [:session :userid] userid)
    (response {:success false})))

(defn logout []
  (assoc (redirect "/") :session nil))

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

;; Landing page

(html/defsnippet landing-page-template
  "layout.html" [:article] [])

(defn landing-page
  [request]
  (layout request (landing-page-template)))
