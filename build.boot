(set-env!
 :source-paths #{"src/cljs"}
 :dependencies '[[org.clojure/clojure "1.9.0-alpha17"]
                 [org.clojure/clojurescript"1.9.562"]
                 [adzerk/boot-cljs "2.0.0"]])

(require '[adzerk.boot-cljs :refer [cljs]]
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
   (cljs)
   (replace-main)
   (target :dir #{"target"})))
