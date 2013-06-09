(ns justapp.auth
  (:require [monger.collection :as mc]
            [ring.util.response :refer [response]]
            [justapp.util :as util])
  (:import org.bson.types.ObjectId
           org.mindrot.jbcrypt.BCrypt))

(defn create-user
  [email password & [:keys [roles] :or {roles #{::member}}]]
  (mc/insert "users"
             {:_id (ObjectId.)
              :email email
              :password (BCrypt/hashpw password (BCrypt/gensalt))
              :roles roles}))

(defn find-user-by-id
  [id]
  (and id (mc/find-map-by-id "users" (ObjectId. id))))

(defn find-user-by-email
  [email]
  (mc/find-one-as-map "users" {:email email}))

(defn drop-user
  [userid]
  (mc/remove-by-id "users" (ObjectId. userid)))

(defn wrap-authentication
  [app]
  (fn [req]
    (if-let [user (find-user-by-id (:userid (:session req)))]
      (-> (assoc req :user user)
          (app)
          (assoc :session (:session req)))
      (app req))))

(defn save-credentials
  [userid email password roles]
  (mc/update-by-id "users"
                   (ObjectId. userid)
                   {"$set" {:email email
                            :password (BCrypt/hashpw password (BCrypt/gensalt))
                            :roles roles}}))

(defn verify-credentials
  [email password]
  (if-let [user (find-user-by-email email)]
    (when (BCrypt/checkpw password (:password user))
      user))))
