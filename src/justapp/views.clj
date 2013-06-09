(ns justapp.views
  (:require [monger.collection :as mc]
            [net.cgrand.enlive-html :as html]
            [ring.util.response :refer [response redirect]]
            [justapp.util :as util]
            [justapp.auth :as auth])
  (:import org.bson.types.ObjectId
           java.util.Date))

;; Layout

(html/defsnippet menu-authenticated
  "layout.html" [:#menu-authenticated]
  [user]
  [:#user-profile] (html/content (auth/user-title user)))

(html/defsnippet menu-anonymous
  "layout.html" [:#menu-anonymous] [])

(defn- menu
  [req]
  (if-let [user (:user req)]
    (menu-authenticated user)
    (menu-anonymous)))

(html/deftemplate layout "layout.html"
  [req content]
  [:#menu] (html/content (menu req))
  [:#flash] (html/content (:flash req))
  [:#main] (html/content content)
  [:script] (fn [el] (update-in el [:attrs :src] #(str "/" %)))
  [:link] (fn [el] (update-in el [:attrs :href] #(str "/" %))))

;; Sign Up

(html/defsnippet signup-form-template
  "layout.html" [:#signup-form] [])

(defn signup-form
  [req]
  (layout req (signup-form-template)))

(defn signup-post
  [email]
  (if (and (not (auth/find-user email))
           (not (auth/signup-exists? email)))
    (do (auth/signup-start email)
        (-> (redirect "/")
            (assoc :flash "We've sent you a confirmation code!
                           Please check your email.")))
    (-> (response "")
        (assoc :flash "This address is already exist."))))

(html/defsnippet signup-confirm-template
  "signup_confirm.html"
  [:form]
  [email code]
  [:#hidden-email] (html/set-attr :value email)
  [:#hidden-code] (html/set-attr :value code))

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
              (auth/create-user email p1)
              (-> (redirect "/")
                  (assoc :flash "Your account has been created.")))
          (layout (signup-confirm-template email code)))
        (redirect "/"))
      (redirect "/"))))

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

(html/defsnippet frontpage-template
  "frontpage.html" [:article] [])

(defn frontpage
  [req]
  (layout req (frontpage-template)))
