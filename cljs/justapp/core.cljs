(ns justapp.core
  (:require [clojure.browser.repl]))

(clojure.browser.repl/connect "http://localhost:9000")

(defn futurama
  [value]
  (js/alert value))

(futurama value)
