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
            [justapp.config :as config]
            [justapp.util :as util]
            [justapp.handlers :as handlers]
            [justapp.auth :as auth]))

(defroutes routes
  (GET "/" req (handlers/frontpage req))

  (GET "/signup" req (handlers/signup-form req))
  (POST "/signup" [email] (handlers/signup-post email))
  ;(ANY "/signup-confirm" req (handlers/signup-confirm req))

  ;(GET "/loginform" [] (handlers/login-form))
  ;(POST "/loginform" req (handlers/login-post req))
  ;(GET "/logout" [] (handlers/logout))

  ;(GET "/profile" req (handlers/profile-form req))
  ;(POST "/profile" req (handlers/profile-post req))

  ;(GET "/_dummy" [] {:headers {"Content-Type" "text/javascript"}})
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

(connect-via-uri! (:mongodb-uri config/config))

(defn start
  [& {:keys [port join?]
      :or {port 7777, join? false}}]
  (run-jetty #'app {:port port :join? join?}))

(defn -main [port]
  (start :port (Integer. port)))
