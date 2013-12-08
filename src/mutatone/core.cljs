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

;; function randomBetween(m, n)
;;     -- Returns a pseudorandom real number in the range  m, n .
;;     return math.random() * (n - m) + m
;; end
;; 
;; function logRandomBetween(m, n)
;;     -- Like `randomBetween()`, but for logarithmic quantities like
;;     -- frequencies. The log of the return value will be equally
;;     -- likely to lie at any point between log(m) and log(n).
;;     return math.exp(randomBetween(math.log(m), math.log(n)))
;; end

(defn random-between
  "Returns a pseudorandom real number in the range [m, n)."
  [m n]
  (+ m
     (* (rand) (- n m))))

(defn log-random-between
  "Like random-between but for logarithmic quantities. Log of return value
  will be equally distributed between log(m) and log(n)."
  [m n]
  (Math/exp (random-between (Math/log m) (Math/log n))))

(defn possibly-mutate-number [x]
  (let [make-negative? (= (rand-int 2) 0)
        sign (if make-negative? -1 1)
        sign (if (= (rand-int 3) 0) sign 0)
        ]
    (+ x (* sign (Math/round (- 3 (log-random-between 1 3)))))))

(defn possibly-mutate-intervals [intervals]
  (map possibly-mutate-number intervals))

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
(declare set-immediate)

(defn breed! []
  (.log js/console "breeding from breeders:" @breeders)
  (when (= (count @breeders) 2)
    (reset! melodies
      [(breed-from (nth @breeders 0) (nth @breeders 1))
       (breed-from (nth @breeders 0) (nth @breeders 1))
       (breed-from (nth @breeders 0) (nth @breeders 1))
       (breed-from (nth @breeders 0) (nth @breeders 1))
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
