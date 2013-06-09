(ns justapp.views
  (:require [monger.collection :as mc]
            [net.cgrand.enlive-html :as html]
            [ring.util.response :refer [response redirect]]
            [justapp.util :as util]
            [justapp.auth :as auth]
            [justapp.mail :as mail])
  (:import org.bson.types.ObjectId
           java.util.Date))

(html/defsnippet topbar-authenticated
  "layout.html" [:#topbar-authenticated]
  [user]
  [:#user-profile] (html/content (auth/user-title user)))

(html/defsnippet topbar-anonymous
  "layout.html" [:#topbar-anonymous] [])

(defn- topbar
  [req]
  (if-let [user (:user req)]
    (topbar-authenticated user)
    (topbar-anonymous)))

(html/deftemplate layout "layout.html"
  [req content]
  [:script] (fn [el] (update-in el [:attrs :src] #(str "/" %)))
  [:link] (fn [el] (update-in el [:attrs :href] #(str "/" %)))
  [:#main] (html/content content)
  [:#topbar] (html/content (topbar req)))

(html/defsnippet frontpage-template
  "frontpage.html" [:article] [])

(defn frontpage
  [req]
  (layout req (frontpage-template)))

;; Sign Up

(html/deftemplate signup-form-template
  "signup_form.html" [])

(defn signup-form []
  (apply str (signup-form-template)))

(defn signup-post
  [email]
  (if (and (= 0 (.length (mc/find "users" {:email email})))
           (= 0 (.length (mc/find "signup" {:email email}))))
    (let [code (util/random-string 16)]
      (mc/insert "signup" {:_id (ObjectId.) :email email :code code})
      (mail/sendmail email
                     "Registration on Justapp"
                     (mail/signup-mail email code))
      (response {:success true}))
    (response {:success false})))

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

(defn person-id
  [{session :session}]
  (response {:id (:userid session)}))

(defn- user-title
  [user]
  (let [f (:firstname user)
        l (:lastname user)]
    (if (and f l)
      (str f " " l)
      (:email user))))

(defn person
  [req]
  (let [user (:user req)]
    (response
     {:roles (:roles user)
      :title (user-title user)})))

(html/deftemplate login-form-template
  "loginform.html" [])

(defn login-form []
  (apply str (login-form-template)))

(defn login-post
  [{params :params session :session}]
  (if-let [userid (auth/verify-credentials (:email params) (:password params))]
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
