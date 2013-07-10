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
            [monger.ring.session-store :refer [session-store]]
            [cemerick.friend :refer [authenticate logout*]]
            [cemerick.friend.workflows :refer [interactive-form]]
            [cemerick.friend.credentials :refer [bcrypt-credential-fn]]
            [ilshad.layout :refer [wrap-layout prevent-layout]]
            [justapp.layout :refer [layout-template]]
            [justapp.cfg :refer [config]]
            [justapp.util :refer [wrap-utf8]]
            [justapp.auth :refer [find-user]]
            [justapp.handlers :as handlers]))

(defroutes app*
  (GET "/" _ (handlers/landing-page))
  (GET "/signup" _ (handlers/signup-form))
  (POST "/signup" [email] (handlers/signup-post email))
  (ANY "/signup-confirm" [email code password] (handlers/signup-confirm email code password))
  (GET "/login" req (handlers/login-form req))
  (GET "/logout" req (logout* (redirect (str (:context req) "/"))))
  (GET "/profile" req (handlers/profile-form req))
  (POST "/profile" req (handlers/profile-post req))
  (prevent-layout (files "/static" {:root "resources/static"}))
  (not-found "<h1>Not Found</h1>"))

(def app
  (-> #'app*
      (wrap-layout layout-template)
      ;(wrap-layout {:default layout-template})
      wrap-json-response
      wrap-utf8
      (authenticate {:credential-fn (partial bcrypt-credential-fn find-user)
                     :workflows [(interactive-form)]})
      (site {:session {:store (session-store)
                       :cookie-name "SID"
                       :cookie-attrs {:expires "Mon, 13-Apr-2020 12:00:00 GMT"}}})))

(connect-via-uri! (:mongodb-uri config))

(defn start
  [& {:keys [port join?]
      :or {port 7777, join? false}}]
  (run-jetty #'app {:port port :join? join?}))

(defn -main [port]
  (start :port (Integer. port)))
