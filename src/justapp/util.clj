(ns justapp.util
  (:require [ring.util.response :refer [charset
                                        response]]
            [ring.util.codec :refer [percent-decode]]
            [monger.collection])
    (:import org.mindrot.jbcrypt.BCrypt))

(defn wrap-utf8
  [app]
  (fn [req]
    (charset (app req) "utf-8")))

(defn wrap-debug
  [app]
  (fn [request]
    (let [resp (app request)]
      (println "\n#########################")
      (println "\nREQUEST :: " request)
      (println "\nRESPONSE :: " resp)
      (println "\n\n\n")
      resp)))

(defn- maybe-nil [v]
  (if (or (= v "null") (= v "")) nil v))

(defn handle-input-param
  [input]
  (-> input
      percent-decode
      clojure.string/trim
      maybe-nil))

(defn handle-input-body
  [input]
  (-> input
      (slurp :encoding "UTF-8")
      clojure.string/trim
      maybe-nil))

(defn handle-param [v]
  (maybe-nil v))

(def lorem-ipsum "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque in sapien neque, quis rhoncus justo. Cras condimentum lacus ac eros lobortis commodo. Nunc lacinia nisl eu elit porttitor malesuada. Donec id erat diam, et feugiat urna. Quisque tortor massa, eleifend et pretium elementum, auctor ac augue. Phasellus ac erat non leo tincidunt dapibus pulvinar ac eros. Ut porta mauris sit amet eros tristique ac pretium erat blandit.")

(defn random-string [length]
  (let [ascii-codes (concat (range 48 58) (range 66 91) (range 97 123))]
    (apply str (repeatedly length #(char (rand-nth ascii-codes))))))

(defn hash-bcrypt
  [password]
  (BCrypt/hashpw password (BCrypt/gensalt)))

(defn bcrypt-verify
  [password hash]
  (BCrypt/checkpw password hash))

(defn cutoff
  [length v]
  (if (< length (count v))
    (str (apply str (take length v)) "...")
    v))
