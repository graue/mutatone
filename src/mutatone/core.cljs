(ns mutatone.core
  (:require [clojure.browser.repl :as repl]
            [mutatone.theory :as t]
            [mutatone.audio :as a]
            [hum.core :as hum]
            [mutatone.dom :as dom]))

(def ctx (atom nil))
(def osc (atom nil))
(def filt (atom nil))
(def gain (atom nil))

(defn audio-inited? []
  (not (nil? @ctx)))

(defn init-audio []
  (when (audio-inited?)
    (throw "Audio already initialized"))
  (reset! ctx (hum/create-context))
  (reset! osc (hum/create-osc @ctx :triangle))
  (set! (.-type @osc) "triangle")  ; Otherwise we get a sine in Firefox...?
  (reset! filt (hum/create-biquad-filter @ctx))
  (reset! gain (hum/create-gain @ctx))
  (hum/connect @osc @filt)
  (hum/connect @filt @gain)
  (hum/start-osc @ctx @osc)
  (hum/connect-output @ctx @gain)
  nil)

(defn panic []
  (a/kill-notes @osc @gain))

(def melodies
  (atom
    [{:intervals [0 0 1 1 3 2 -1 -1 0 0 1 1 3 2 -1 -1]
      :scale "phrygian"
      :root "b"}

     {:intervals [0 0 -1 -1 -2 -2 -3 -3 0 0 2 2 1 1 1 1]
      :scale "minor pentatonic"
      :root "c"}

     {:intervals [0 1 2 1 4 5 3 0 2 2 -1 -2 -1 -1 0 0 0 0 0 0]
      :scale "major"
      :root "d#"}]))

(defn init-page []
  (dom/render-melodies @melodies))

(defn on-load [cb]
  (if (#{"complete" "loaded" "interactive"} (.-readyState js/document))
    (.setImmediate js/window cb)
    (.addEventListener js/document "DOMContentLoaded" init-page false)))

(on-load init-page)

(repl/connect "http://localhost:9000/repl")

(comment
  (play-notes @osc @gain
              (t/scalify [0 0 1 1 3 2 -1 -1] "major pentatonic" "d#" 6)
              0.75)
  )
