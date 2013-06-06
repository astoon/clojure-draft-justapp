(ns justapp.xhr
  (:require [monger.collection :as mc]
            [monger.query :as mq]
            [net.cgrand.enlive-html :as html]
            [ring.util.response :refer [response
                                        redirect]]
            [justapp.util :refer [handle-input-body
                                handle-input-param
                                handle-param
                                random-string
                                cutoff]]
            [justapp.auth :refer [create-asecret
                                verify-credentials]]
            [justapp.mail :refer [sendmail
                                signup-mail]]]
            justapp.action)
  (:import org.bson.types.ObjectId
           java.util.Date))

;;;;;;;;;; Authenticate

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

(defn signup-form []
  (apply str (signup-form-content)))

(defn signup-post
  [email]
  (if (and (= 0 (.length (mc/find "users" {:email email})))
           (= 0 (.length (mc/find "signup" {:email email}))))
    (let [code (random-string 16)]
      (mc/insert "signup" {:_id (ObjectId.) :email email :code code})
      (sendmail email "Registration on Justapp" (signup-mail email code))
      (response {:success true}))
    (response {:success false})))

(html/deftemplate login-form-content
  "loginform.html" [])

(defn login-form []
  (apply str (login-form-content)))

(defn login-post
  [{params :params session :session}]
  (if-let [userid (verify-credentials (:email params) (:password params))]
    (do (pull-session userid (:userid session) "end")
        (assoc-in (response {:success true}) [:session :userid] userid))
    (response {:success false})))

(defn logout []
  (assoc (redirect "/") :session nil))

(defn make-project
  [{params :params session :session}]
  (response {:entry-id (:id params) :create (:create params)}))

;;;;;;;;;; Profile

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
