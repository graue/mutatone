(ns mutatone.audio
  (:require [hum.core :as hum]
            [mutatone.theory :as t]))

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
  [osc gain-node phrase length & [adsr]]
  (let [ctx (.-context osc)
        start (+ 0.15 (hum/curr-time ctx))
        raw-notes (t/phrase->notes phrase length start)]
    (doseq [note raw-notes]
      (play-note-with-adsr osc gain-node (:freq note) 1.0
                           (:start note) length
                           (or adsr default-adsr)))))

(defn kill-notes
  "Kill all notes playing or scheduled to play on this oscillator/gain pair."
  [osc gain-node]
  (let [time (hum/curr-time (.-context gain-node))]
    (.cancelScheduledValues (.-frequency osc) time)
    (.cancelScheduledValues (.-gain gain-node) time)
    (.setValueAtTime (.-gain gain-node) 0 time)))
