(ns justapp.mail
  (:require [net.cgrand.enlive-html :as html]
            [justapp.cfg :refer [cfg]])
  (:import org.apache.commons.mail.HtmlEmail))

(defn sendmail
  [address subject content]
  (doto (HtmlEmail.)
    (.setHostName "smtp.gmail.com")
    (.setSslSmtpPort "587")
    (.setTLS true)
    (.addTo address)
    (.setFrom "xxx@xxx.com" "Justapp")
    (.setSubject subject)
    (.setHtmlMsg content)
    (.setAuthentication "xxx@xxx.com" "xxx")
    (.send)))

(defn- signup-confirm-link [addr code]
  (str (:root-url cfg) "/signup-confirm?email=" addr "&code=" code))

(html/deftemplate signup-mail-template
  "mail/signup.html"
  [addr code]
  [:#link] (html/set-attr :href (signup-confirm-link addr code))
  [:#link] (html/content (signup-confirm-link addr code))
  [:#addr] (html/content addr))

(defn signup-mail
  [addr code]
  (apply str (signup-mail-template addr code)))
