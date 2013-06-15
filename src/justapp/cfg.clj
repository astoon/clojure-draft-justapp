(ns justapp.cfg
  (:require [environ.core :refer [env]]))

(def config {:root-url (env :env-root-url)
             :mongodb-uri (env :env-mongodb-uri)
             :smtp-addrfrom (env :justapp-smtp-addrfrom)
             :smtp-namefrom (env :justapp-smtp-namefrom)
             :smtp-username (env :justapp-smtp-username)
             :smtp-password (env :justapp-smtp-password)})
