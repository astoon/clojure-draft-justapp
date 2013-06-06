(ns justapp.core-test
  (:use clojure.test
        kerodon.test
        kerodon.core
        justapp.core))

(deftest anyone-can-view-frontpage
  (-> (session app)
      (visit "/")
      (has (text? "Hello World"))))
