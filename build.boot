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
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
         '[clojure.java.io :as io])


(deftask replace-main
  "Modify main.js to simply `goog.require` the CLJS entrypoint it refers to
(looks like 'boot.cljs.main516')."
  []
  (with-pre-wrap fileset
    (let [tmp-dir (tmp-dir!)]
      (spit (io/file tmp-dir "main.js")
            (str "goog.require('"
                 (get (->> "main.js"
                           (tmp-get fileset)
                           (tmp-file)
                           slurp
                           (re-find #"(boot.cljs.\w+)\"")) 1)
                 "');"))
      (-> fileset (add-resource tmp-dir) (commit!)))))


(deftask dev
  "Start development environment"
  []
  (comp
   (watch)
   (reload :port 8079
           :ws-host "localhost")
   (cljs-repl)
   (cljs)
   (replace-main)
   (target :dir #{"target"})))
