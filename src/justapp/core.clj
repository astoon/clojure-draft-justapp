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
            [cemerick.friend :refer [authenticate]]
            [cemerick.friend.workflows :refer [interactive-form]]
            [justapp.cfg :refer [config]]
            [justapp.util :as util]
            [justapp.handlers :as handlers]
            [justapp.auth :as auth]))

(defroutes routes
  (GET "/" request (handlers/landing-page request))

  (GET "/signup" request (handlers/signup-form request))
  (POST "/signup" request (handlers/signup-post request))
  (ANY "/signup-confirm" request (handlers/signup-confirm request))

  (GET "/login" request (handlers/login-form request))
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
                       :cookie-attrs {:expires "Mon, 13-Apr-2020 12:00:00 GMT"}}})
      (authenticate {:allow-anon? true
                     :login-uri "/login"
                     :default-landing-uri "/"
                     :credential-fn #(auth/authenticate (:username %1)
                                                        (:password %1))
                     :workflows [(interactive-form)]})))

(connect-via-uri! (:mongodb-uri config))

(defn start
  [& {:keys [port join?]
      :or {port 7777, join? false}}]
  (run-jetty #'app {:port port :join? join?}))

(defn -main [port]
  (start :port (Integer. port)))
