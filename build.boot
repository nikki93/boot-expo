(merge-env!
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
         '[clojure.java.io :as io]
         '[clojure.string :as str])

(deftask ^:private write-module-map
  "Parse `(js/require \"module\")` usages and output a JavaScript object of requires.
Must be run before ClojureScript compilation."
  []
  (let [validate-module (fn [x]
                          (if (string? x)
                            x
                            (-> (format "Invalid module: `%s`. `js/require` can only process strings." x)
                                (ex-info {:module x})
                                (throw))))
        parse-tmpfile (fn [file]
                        (->> file
                             tmp-file
                             slurp
                             (re-seq #"\(js/require([^\)]+)\)")
                             (map (comp pr-str
                                        validate-module
                                        read-string
                                        second))))
        dir (tmp-dir!)
        file (io/file dir "moduleMap.js")]
    (with-pre-wrap fileset
      (->> fileset
           input-files
           (by-ext [".clj" ".cljs" ".cljc"])
           (mapcat parse-tmpfile)
           distinct
           sort
           (map #(format "  %s: require(%s)," %1 %1))
           (str/join \newline)
           (format "module.exports = {\n%s\n};")
           (spit file))
      (-> fileset
          (add-resource dir)
          commit!))))

(deftask ^:private write-config
  "Writes config to config.json resource file."
  [c config VAL edn "Config EDN"]
  (let [dir (tmp-dir!)
        file (io/file dir "config.json")]
    (with-pre-wrap fileset
      (spit file (json-generate config {:pretty true}))
      (-> fileset
          (add-resource dir)
          commit!))))

(deftask dev
  "Start development environment"
  []
  (let [hostname (.getHostName (java.net.InetAddress/getLocalHost))]
    (comp
     (watch)
     (reload :ip "0.0.0.0" :ws-host hostname)
     (write-config :config {:dev true})
     (write-module-map)
     (cljs-repl :ip "0.0.0.0" :ws-host hostname)
     (cljs :compiler-options {:infer-externs true
                              :language-in :ecmascript5
                              :language-out :no-transpile
                              :parallel-build true}
           :optimizations :none
           :source-map false)
     (target :dir #{"target"}))))

(deftask prod
  "Build production environment"
  []
  (comp
   (write-config :config {:dev false})
   (write-module-map)
   (cljs :compiler-options {:closure-defines {'goog.DEBUG false}
                            :infer-externs true
                            :language-in :ecmascript5
                            :language-out :no-transpile
                            :parallel-build true}
         :optimizations :advanced
         :source-map true)
   (target :dir #{"target"})))
