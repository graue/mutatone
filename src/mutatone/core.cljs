(ns mutatone.core
  (:require [clojure.browser.repl :as repl]
            [mutatone.theory :as t]
            [mutatone.audio :as a]
            [hum.core :as hum]
            [mutatone.dom :as dom]
            [mutatone.melodies :as m]))

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
  (atom m/seed-melodies))

(def breeders
  (atom []))

(declare play)
(declare add-breeder)

(defn set-immediate [func]
  (.setTimeout js/window func 0))

(defn breed! []
  (when (= (count @breeders) 2)
    (reset! melodies
      [(m/breed-from (nth @breeders 0) (nth @breeders 1))
       (m/breed-from (nth @breeders 0) (nth @breeders 1))
       (m/breed-from (nth @breeders 0) (nth @breeders 1))
       (m/breed-from (nth @breeders 0) (nth @breeders 1))
       (m/breed-from (nth @breeders 0) (nth @breeders 1))
       (m/breed-from (nth @breeders 0) (nth @breeders 1))])
    (reset! breeders [])
    (set-immediate #(dom/render-melodies @melodies play add-breeder))))

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
    (set-immediate cb)
    (.addEventListener js/document "DOMContentLoaded" init-page false)))

(on-load init-page)

#_(repl/connect "http://localhost:9000/repl")
