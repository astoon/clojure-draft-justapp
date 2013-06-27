(ns justapp.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [ring.util.response :refer [redirect]]
            [compojure.core :refer [defroutes ANY GET POST DELETE PUT]]
            [compojure.route :refer [files not-found]]
            [compojure.handler :refer [site]]
            [monger.core :refer [connect-via-uri!]]
            [monger.ring.session-store :refer [monger-store]]
            [cemerick.friend :refer [authenticate logout*]]
            [cemerick.friend.workflows :refer [interactive-form]]
            [cemerick.friend.credentials :refer [bcrypt-credential-fn]]
            [justapp.cfg :refer [config]]
            [justapp.util :as util]
            [justapp.handlers :as handlers]
            [justapp.layout :refer [layout wrap-layout]]
            [justapp.auth :refer [find-user]]))

(defroutes app*
  (GET "/" req (handlers/landing-page req))
  (GET "/signup" req (handlers/signup-form req))
  (POST "/signup" req (handlers/signup-post req))
  (ANY "/signup-confirm" req (handlers/signup-confirm req))
  (GET "/login" req (handlers/login-form req))
  (GET "/logout" req (logout* (redirect (str (:context req) "/"))))
  ;(GET "/profile" req (handlers/profile-form req))
  ;(POST "/profile" req (handlers/profile-post req))
  (files "/static" {:root "resources/static"})
  (not-found (layout nil "Not found")))

(def app
  (-> #'app*
      ;wrap-layout
      ;wrap-json-response
      ;util/wrap-utf8
      ;(authenticate {:credential-fn (partial bcrypt-credential-fn find-user) :workflows [(interactive-form)]})
      (site {:session {:store (monger-store)
                       :cookie-name "SID"
                       :cookie-attrs {:expires "Mon, 13-Apr-2020 12:00:00 GMT"}}})))

(connect-via-uri! (:mongodb-uri config))

(defn start
  [& {:keys [port join?]
      :or {port 7777, join? false}}]
  (run-jetty #'app {:port port :join? join?}))

(defn -main [port]
  (start :port (Integer. port)))
