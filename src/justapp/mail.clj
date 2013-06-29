(ns justapp.mail
  (:require [net.cgrand.enlive-html :as html]
            [postal.core :refer [send-message]]
            [justapp.cfg :refer [config]]))

(defn sendmail
  [address subject content]
  (future
    (send-message ^{:host "smtp.gmail.com"
                    :port 587
                    :user (:smtp-username config)
                    :pass (:smtp-password config)
                    :tls true}
                  {:from (:smtp-addrfrom config)
                   :to address
                   :subject subject
                   :body [{:type "text/html"
                           :content content}]})))

(defn- signup-confirm-link [addr code]
  (str (:root-url config) "/signup-confirm?email=" addr "&code=" code))

(html/deftemplate signup-mail-template
  "mail/signup.html"
  [addr code]
  [:#link] (html/set-attr :href (signup-confirm-link addr code))
  [:#link] (html/content (signup-confirm-link addr code))
  [:#addr] (html/content addr))

(defn signup-mail
  [addr code]
  (apply str (signup-mail-template addr code)))
