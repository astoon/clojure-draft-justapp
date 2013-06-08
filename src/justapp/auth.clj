(ns justapp.auth
  (:require [monger.collection :as mc]
            [ring.util.response :refer [response]]
            [justapp.util :refer [random-string
                                  hash-bcrypt
                                  bcrypt-verify]])
  (:import org.bson.types.ObjectId))

(defn get-user [id]
  (and id (mc/find-map-by-id "users" (ObjectId. id))))

(defn drop-user
  [userid]
  (mc/remove "sessions" {:data {:userid userid}})
  (mc/remove-by-id "users" (ObjectId. userid)))

(defn save-credentials
  [id email password roles]
  (mc/update-by-id "users"
                   (ObjectId. id)
                   {"$set" {:email email
                            :password (hash-bcrypt password)
                            :roles roles}}))

(defn verify-credentials
  [email password]
  (if-let [user (mc/find-one-as-map "users" {:email email})]
    (if (bcrypt-verify password (:password user))
      (.toString (:_id user)))))

(defn create-authenticated-user
  [email password]
  (mc/insert "users" {:_id (ObjectId.)
                      :email email
                      :password (hash-bcrypt password)
                      :roles #{::user}}))

(defn- update-session
  [resp req]  
  (if (and (= (:uri req) "/")
           (not (:userid (:session resp))))
    (assoc resp :session (:session req))
    resp))

(defn wrap-authentication
  [app]
  (fn [req]
    (if-let [user (get-user (:userid (:session req)))]
      (-> (assoc req :user user)
          (app)
          (update-session req)))))
