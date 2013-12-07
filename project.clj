(defproject mutatone "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2080"]
                 [hum "0.2.4"]
                 [prismatic/dommy "0.1.2"]]

  :plugins [[lein-cljsbuild "1.0.0"]]

  :source-paths ["src"]

  :cljsbuild { 
    :builds [{:id "mutatone"
              :source-paths ["src"]
              :compiler {
                :output-to "mutatone.js"
                :output-dir "out"
                :optimizations :none
                :source-map true}}]})
