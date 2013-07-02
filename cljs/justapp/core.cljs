(ns justapp.core
  (:require [clojure.browser.repl :as repl]))

(repl/connect "http://localhost:9000/repl")

(defn futurama
  [value]
  (js/alert value)
  (js/alert "So, I'm done..."))

(futurama "Welcome!")
