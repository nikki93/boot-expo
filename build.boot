(set-env!
 :source-paths #{"src/cljs"}
 :dependencies '[[org.clojure/clojure "1.9.0-alpha17"]
                 [org.clojure/clojurescript"1.9.562"]
                 [adzerk/boot-cljs "2.0.0"]
                 [adzerk/boot-reload "0.5.1"]
                 [adzerk/boot-cljs-repl "0.3.3"]
                 [com.cemerick/piggieback "0.2.1"]
                 [weasel "0.7.0"]
                 [org.clojure/tools.nrepl "0.2.12"]])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]])


(deftask dev
  "Start development environment"
  []
  (let [hostname (.getHostName (java.net.InetAddress/getLocalHost))]
    (comp
     (watch)
     (reload :ip "0.0.0.0" :ws-host hostname)
     (cljs-repl :ip "0.0.0.0" :ws-host hostname)
     (cljs)
     (target :dir #{"target"}))))
