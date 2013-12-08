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

(defn possibly-mutate-intervals [x]
  x ; not done
  )

(defn mix-vectors [v1 v2]
  (.log js/console "mix vectors start")
  (loop [v1 v1, v2 v2, acc []]
    (if (= (rand-int 4) 0)
      (recur v2 v1 acc)
      (if (empty? v1)
        (do (.log js/console "mix vectors done") acc)
        (recur (rest v1) (rest v2) (conj acc (first v1)))))))

(defn breed-from [m1 m2]
  {:intervals (possibly-mutate-intervals
                (mix-vectors (:intervals m1) (:intervals m2)))
   :scale (if (= 0 (rand-int 2))
            (:scale m1)
            (:scale m2))
   :root (if (= 0 (rand-int 2))
           (:root m1)
           (:root m2))})

(def breeders
  (atom []))

(declare play)
(declare add-breeder)

(defn breed! []
  (.log js/console "breeding from breeders:" @breeders)
  (when (= (count @breeders 2))
    (reset! melodies
      [(breed-from (nth @breeders 0) (nth @breeders 1))
       (breed-from (nth @breeders 0) (nth @breeders 1))
       (breed-from (nth @breeders 0) (nth @breeders 1))])
    (reset! breeders [])
    (.log js/console "new things:" @melodies)
    (set-immediate #(dom/render-melodies @melodies play add-breeder)))
  )

(defn set-immediate [func]
  (.setTimeout js/window func 0))

(defn add-breeder [idx]
  "Called when Breed button clicked for melody at idx."
  (when (< (count @breeders) 2)
    (dom/disable-breed-button idx)
    (swap! breeders conj (nth @melodies idx))
    (when (= (count @breeders) 2)
      (set-immediate breed!))))

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
  (dom/render-melodies @melodies play add-breeder))

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
