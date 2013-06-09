(ns justapp.auth
  (:require [monger.collection :as mc]
            [ring.util.response :refer [response]]
            [justapp.util :as util]
            [justapp.mail :as mail])
  (:import org.bson.types.ObjectId
           org.mindrot.jbcrypt.BCrypt))

(defrecord User [email password
                 firstname lastname
                 roles])

(defn create-user
  [email password & {:keys [firstname lastname roles]}]
  (mc/insert "users"
             {:_id (ObjectId.)
              :email email
              :password (BCrypt/hashpw password (BCrypt/gensalt))
              :firstname (or firstname "")
              :lastname (or lastname "")
              :roles (or roles #{::member})}))

(defn find-user
  [email]
  (if-let [u (mc/find-one-as-map "users" {:email email})]
    (map->User u)))

(defn wrap-authentication
  [app]
  (fn [req]
    (if-let [user (find-user (:userid (:session req)))]
      (-> (assoc req :user user)
          (app)
          (assoc :session (:session req)))
      (app req))))

(defn authenticate
  [email password]
  (if-let [user (find-user email)]
    (when (BCrypt/checkpw password (:password user))
      user)))

(defn user-title
  [user]
  (let [name (str (:firstname user) " " (:lastname user))]
    (if (empty? (clojure.string/trim name))
      (:email user)
      name)))

(defn signup-start
  [email]
  (let [code (util/random-string 16)]
    (mc/insert "signup" {:_id (ObjectId.)
                         :email email
                         :code code})
    (mail/sendmail email
                   "Registration on Justapp"
                   (mail/signup-mail email code))))

(defn signup-exists?
  [email]
  (mc/find-one-as-map "signup" {:email email}))

(defn signup-end
  [email code])
