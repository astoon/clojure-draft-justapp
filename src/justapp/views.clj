(ns justapp.views
  (:require [monger.collection :as mc]
            [monger.query :as mq]
            [net.cgrand.enlive-html :as html]
            [ring.util.response :refer [response redirect]]
            [justapp.util :as util]
            [justapp.auth :as auth]
            [justapp.mail :as mail])
  (:import org.bson.types.ObjectId
           java.util.Date))

(html/defsnippet frontpage-content
  "frontpage.html" [:#content] [])

(defn frontpage []
  (layout/layout (frontpage-content)))

(html/defsnippet signup-confirm-content
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
              (auth/create-authenticated-user email p1)
              (-> (redirect "/")
                  (assoc :flash "Your account has been created.")))
          (layout/layout (signup-confirm-content email code)))
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

(html/deftemplate signup-form-content
  "signup_form.html" [])

(defn signup-form []
  (apply str (signup-form-content)))

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

(html/deftemplate login-form-content
  "loginform.html" [])

(defn login-form []
  (apply str (login-form-content)))

(defn login-post
  [{params :params session :session}]
  (if-let [userid (auth/verify-credentials (:email params) (:password params))]
    (assoc-in (response {:success true}) [:session :userid] userid)
    (response {:success false})))

(defn logout []
  (assoc (redirect "/") :session nil))

(defn make-project
  [{params :params session :session}]
  (response {:entry-id (:id params) :create (:create params)}))

(html/deftemplate profile-form-content "profile.html"
  [firstname lastname]
  [:#profile-firstname] (html/set-attr :value firstname)
  [:#profile-lastname] (html/set-attr :value lastname))

(defn profile-form
  [{user :user}]
  (apply str (profile-form-content (:firstname user)
                                   (:lastname user))))

(defn profile-post
  [{params :params user :user}]
  (mc/update-by-id "users"
                   (:_id user)
                   {"$set" {:firstname (:firstname params)
                            :lastname (:lastname params)}})
  {:status 204})
