(ns justapp.cfg
  (:require [environ.core :refer [env]]))

(def cfg {:root-url (env :env-root-url)
          :mongodb-uri (env :env-mongodb-uri)})
