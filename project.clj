(defproject justapp "0.1.0-SNAPSHOT"
  :url "https://github.com/ilshad/clojure-draft-justapp"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :description "justapp"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [environ "0.4.0"]
                 [ring "1.2.0-beta2"]
                 [ring/ring-json "0.2.0"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.3"]
                 [enlive "1.1.1"]
                 [com.novemberain/monger "1.6.0-beta2"]
                 [cheshire "5.1.1"]
                 [org.mindrot/jbcrypt "0.3m"]
                 [org.apache.commons/commons-email "1.2"]
                 [com.cemerick/friend "0.1.5"]
                 [clojure-complete "0.2.2"]
                 [com.draines/postal "1.10.4"]
                 [com.cemerick/piggieback "0.0.5"]]
  :min-lein-version "2.0.0"
  :repositories [["central-proxy" "http://repository.sonatype.org/content/repositories/central/"]]
  :plugins [[lein-ring "0.8.5"]
            [lein-environ "0.4.0"]
            [lein-cljsbuild "0.3.2"]]
  :hooks [leiningen.cljsbuild]
  :cljsbuild {:builds [{:source-paths ["cljs"]
                        :compiler {:output-to "resources/static/js/main.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}]}
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  :ring {:handler justapp.core/app
         :port 7777
         :nrepl {:start? true :port 9991}}
  :env {:env-root-url "http://localhost:7777"
        :env-mongodb-uri "mongodb://localhost/justapp"
        :justapp-smtp-addrfrom "xxx@xxx.com"
        :justapp-smtp-namefrom "XXX"
        :justapp-smtp-username "xxx"
        :justapp-smtp-password "xxx"}
  :profiles {:dev {:dependencies [[kerodon "0.1.0"]
                                  [ring-mock "0.1.5"]]}})
