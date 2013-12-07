(ns mutatone.core
  (:require [clojure.browser.repl :as repl]
            [mutatone.theory :as t]
            [hum.core :as hum]))

;;;; Utilities

(defn play-note-with-adsr [osc gain-node freq amp time length
                           {:keys [a d s r]}]
  ;; Yes, this has way too many parameters. :|
  (.setValueAtTime (.-frequency osc) freq time)
  (.setValueAtTime (.-gain gain-node) 0 time)
  (.linearRampToValueAtTime (.-gain gain-node) 1 (+ time a))
  (.linearRampToValueAtTime (.-gain gain-node) s (+ time a d))
  (.setValueAtTime (.-gain gain-node) s (+ time length (- r)))
  (.linearRampToValueAtTime (.-gain gain-node) 0 (+ time length)))

(def default-adsr
  {:a 0.05
   :d 0.05
   :s 0.7  ; about -3dB
   :r 0.0625})

(defn play-phrase
  [osc gain-node phrase length]
  (let [ctx (.-context osc)
        start (+ 0.15 (hum/curr-time ctx))
        raw-notes (t/phrase->notes phrase length start)]
    (doseq [note raw-notes]
      (play-note-with-adsr osc gain-node (:freq note) 1.0
                           (:start note) length
                           default-adsr))))

(def ctx (atom nil))
(def osc (atom nil))
(def filt (atom nil))
(def gain (atom nil))

(defn create-osc-and-stuff []
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

(defn panic
  "Kill all notes."
  []
  (let [time (hum/curr-time (.-context @gain))]
    (.cancelScheduledValues (.-frequency @osc) time)
    (.cancelScheduledValues (.-gain @gain) time)
    (.setValueAtTime (.-gain @gain) 0 time)))

(repl/connect "http://localhost:9000/repl")

(comment
  (play-notes @osc @gain
              (t/scalify [0
                        0 3
                        0 3 1
                        0 3 1 -1
                        0 3 1 -1 -2
                          3 1 -1 -2
                            1 -1 -2
                              -1 -2
                                 -2]
                       "major pentatonic" "d#" 6)
              0.75)
  )
