(ns justapp.cfg
  (:require [environ.core :refer [env]]))

(def config {:root-url (env :env-root-url)
             :mongodb-uri (env :env-mongodb-uri)
             :smtp-addr-from (env :mail-addr-from)
             :smtp-name-from (env :mail-name-from)
             :smtp-username (env :mail-user)
             :smtp-password (env :mail-password)})
