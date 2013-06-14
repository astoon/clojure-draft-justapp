(ns justapp.util
  (:require [ring.util.response :refer [charset]]
            [ring.util.codec :refer [percent-decode]]))

(defn wrap-utf8
  [app]
  (fn [request]
    (charset (app request) "utf-8")))

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

(defn random-string [length]
  (let [ascii-codes (concat (range 48 58) (range 66 91) (range 97 123))]
    (apply str (repeatedly length #(char (rand-nth ascii-codes))))))
