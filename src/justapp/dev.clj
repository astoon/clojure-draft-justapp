(ns justapp.dev
  (require cljs.repl
           cljs.repl.browser
           cemerick.piggieback))

(defn cljs-browser-repl []
  (cemerick.piggieback/cljs-repl
   :repl-env (doto (cljs.repl.browser/repl-env :port 9000)
               cljs.repl/-setup)))
