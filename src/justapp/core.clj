(ns justapp.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [ring.middleware.flash :refer [wrap-flash]]
            [compojure.core :refer [defroutes ANY GET POST DELETE PUT]]
            [compojure.route :refer [files not-found]]
            [compojure.handler :refer [site]]
            [monger.core :refer [connect-via-uri!]]
            [monger.ring.session-store :refer [monger-store]]
            [justapp.cfg :refer [cfg]]
            [justapp.util :refer [wrap-utf8 wrap-debug]]
            [justapp.layout :refer [layout]]
            [justapp.views :as views]
            [justapp.auth :refer [wrap-authentication]]))

(defroutes routes
  (GET "/" [] (views/frontpage))

  (GET "/signup" [] (views/signup-form))
  (POST "/signup" [email] (views/signup-post email))
  (ANY "/signup-confirm" req (views/signup-confirm req))

  (GET "/loginform" [] (views/login-form))
  (POST "/loginform" req (views/login-post req))
  (GET "/logout" [] (views/logout))

  (GET "/profile" req (views/profile-form req))
  (POST "/profile" req (views/profile-post req))

  (GET "/_dummy" [] {:headers {"Content-Type" "text/javascript"}})
  (files "/static" {:root "resources/static"})
  (not-found (layout "So bad:(")))

(def app
  (-> #'routes
      wrap-authentication
      wrap-json-response
      wrap-utf8
      (site {:session {:store (monger-store)
                       :cookie-name "SID"
                       :cookie-attrs {:expires "Mon, 13-Apr-2020 12:00:00 GMT"}}})))

(connect-via-uri! (:mongodb-uri cfg))

(defn start
  [& {:keys [port join?]
      :or {port 7777, join? false}}]
  (run-jetty #'app {:port port :join? join?}))

(defn -main [port]
  (start :port (Integer. port)))
