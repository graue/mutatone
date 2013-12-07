(defproject thing1 "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2080"]
                 [hum "0.2.4"]]

  :plugins [[lein-cljsbuild "1.0.0"]]

  :source-paths ["src"]

  :cljsbuild { 
    :builds [{:id "thing1"
              :source-paths ["src"]
              :compiler {
                :output-to "thing1.js"
                :output-dir "out"
                :optimizations :none
                :source-map true}}]})
