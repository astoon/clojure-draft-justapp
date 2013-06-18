(ns justapp.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [compojure.core :refer [defroutes ANY GET POST DELETE PUT]]
            [compojure.route :refer [files not-found]]
            [compojure.handler :refer [site]]
            [monger.core :refer [connect-via-uri!]]
            [monger.ring.session-store :refer [monger-store]]
            [justapp.cfg :refer [config]]
            [justapp.util :as util]
            [justapp.handlers :as handlers]
            [justapp.auth :as auth]))

(defroutes routes
  (GET "/" request (handlers/frontpage request))

  (GET "/signup" request (handlers/signup-form request))
  (POST "/signup" request (handlers/signup-post request))
  ;(ANY "/signup-confirm" request (handlers/signup-confirm request))

  ;(GET "/loginform" [] (handlers/login-form))
  ;(POST "/loginform" request (handlers/login-post request))
  ;(GET "/logout" [] (handlers/logout))

  ;(GET "/profile" request (handlers/profile-form request))
  ;(POST "/profile" request (handlers/profile-post request))

  (files "/static" {:root "resources/static"})
  (not-found (handlers/layout nil "So bad:(")))

(def app
  (-> #'routes
      auth/wrap-authentication
      wrap-json-response
      util/wrap-utf8
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
