(defproject mutatone "0.1.0-SNAPSHOT"
  :description "Generates melodies using evolutionary algorithms"
  :url "https://github.com/graue/mutatone"
  :license {:name "MIT License"
            :url "https://github.com/graue/mutatone/blob/master/MIT-LICENSE.txt"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2173"]
                 [org.clojure/core.async "0.1.278.0-76b25b-alpha"]
                 [hum "0.3.0"]
                 [om "0.5.3"]
                 [sablono "0.2.14"]]

  :plugins [[lein-cljsbuild "1.0.2"]]

  :source-paths ["src"]

  :cljsbuild { 
    :builds [{:id "mutatone"
              :source-paths ["src"]
              :compiler {
                :output-to "mutatone.js"
                :output-dir "out"
                :optimizations :none
                :source-map true}}]})
