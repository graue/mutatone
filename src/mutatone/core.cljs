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

(def breeders
  (atom []))

(defn breed! []
  "This will be the hard part")

(defn add-breeder [idx]
  "Called when Breed button clicked for melody at idx."
  (when (< (count @breeders) 2)
    (dom/disable-breed-button idx)
    (swap! breeders conj (nth @melodies idx))
    (when (= (count @breeders) 2)
      (breed!))))

(defn play [idx]
  "Called when Play button clicked for melody at idx."
  (when-not (audio-inited?)
    (init-audio))
  (panic)  ; Kill any notes currently playing.
  (let [melody (@melodies idx)
        raw-notes (t/scalify (:intervals melody) (:scale melody)
                             (:root melody) 4)]
    (a/play-phrase @osc @gain raw-notes 0.75)))

(defn init-page []
  (dom/render-melodies @melodies play add-breeders))

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
