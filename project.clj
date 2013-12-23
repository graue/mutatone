(defproject mutatone "0.1.0-SNAPSHOT"
  :description "Generates melodies using evolutionary algorithms"
  :url "https://github.com/graue/mutatone"
  :license {:name "MIT License"
            :url "https://github.com/graue/mutatone/blob/master/MIT-LICENSE.txt"}

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2127"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [hum "0.2.4"]
                 [om "0.1.0-SNAPSHOT"]
                 [sablono "0.1.1"]
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
