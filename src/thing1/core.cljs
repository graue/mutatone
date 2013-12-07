(ns thing1.core
  (:require [clojure.browser.repl :as repl]
            [clojure.set :as cset]
            [hum.core :as hum]))

;;; Work around bug in hum/note-on.
(defn my-note-on [ctx output osc freq & time]
  (let [time (get time 0 (hum/curr-time ctx))]
    (.setValueAtTime (.-frequency osc) freq time)
    (.linearRampToValueAtTime (.-gain output) 1.0 (+ time 0.1))))

;;;; utilities

(defn midi->hz
  "Convert a MIDI note number to its frequency in Hz."
  ; freq = 440 * pow(2.0, (note - 69) / 12.0);
  [note-num]
  (* 440 (Math/pow 2.0
                   (/ (- note-num 69) 12.0))))

(defn sum [nums]
  (reduce + 0 nums))

(def scales
  {"major" [2 2 1 2 2 2 1]
   "phrygian" [1 2 2 2 1 2 2]
   "mixolydian" [2 2 1 2 2 1 2]
   "aeolian" [2 1 2 2 1 2 2]
   "harmonic major" [2 2 1 2 1 3 1]
   "harmonic minor" [2 1 2 2 1 3 1]
   "augmented" [2 2 1 3 1 2 1]
   "diminished" [2 1 2 1 3 2 1]
   "octatonic" [2 1 2 1 2 1 2 1]
   "whole tone" [2 2 2 2 2 2]
   "major bebop" [2 2 1 2 1 1 2 1]
   "minor bebop" [2 1 2 2 1 1 2 1]
   "major blues" [2 1 1 3 1 1 3]
   "minor blues" [3 2 1 1 3 1 1]
   "major pentatonic" [2 2 3 2 3]
   "minor pentatonic" [3 2 2 3 2]})

(def notes
  {"c"  0; ,  "b#" 0
   "c#" 1; ,  "db" 1
   "d"  2; 
   "d#" 3; ,  "eb" 3
   "e"  4; ,  "fb" 4
   "f"  5; 
   "f#" 6; ,  "gb" 6
   "g"  7; 
   "g#" 8; ,  "ab" 8
   "a"  9; 
   "a#" 10;, "bb" 10
   "b"  11;, "cb" 11
   })

(def notenum->name
  (cset/map-invert notes))

(defn midi->str
  "Convert a MIDI note number to a string representation.
  Note: by convention, A4 = 440 Hz = 69."
  [note-num]
  (let [num-in-octave (mod note-num 12)
        octave (dec (Math/floor (/ note-num 12)))]
    (str (notenum->name num-in-octave) octave)))

(defn midis->str
  [note-nums]
  (apply str
         (interpose " " (map midi->str note-nums))))
 
(defn dia->chrom
  "Convert diatonic intervals (relative to scale root = 0, *not* each other)
  to chromatic intervals (again relative to the scale root), for the given
  scale."
  [intervals scale-name]
  (let [raw-scale (scales (.toLowerCase scale-name))
        scale (cycle raw-scale)
        neg-scale (cycle (reverse raw-scale))]  ; For negative intervals.
    (if-not (coll? intervals)
      ; "intervals" is actually just one value.
      (if (neg? intervals)
        ; Negative case, e.g. for -2, sum the *last* two intervals in the
        ; scale definition and negate the result.
        (- (sum (take (- intervals) neg-scale)))
        ; Positive case.
        (sum (take intervals scale)))
      ; If passed a collection, map this function across each element.
      (map #(dia->chrom % scale-name) intervals))))

(defn scalify
  "Convert a list of intervals to MIDI note numbers based on the scale
  named `scale` starting on `note` in octave `octave`."
  [intervals scale note & [octave]]
  (let [octave (or octave 4)
        octave-offset (* 12 octave)
        note-offset (notes (.toLowerCase note))
        chromatic-intervals (dia->chrom intervals scale)]
    (map (partial + octave-offset note-offset) chromatic-intervals)))

(. js/console (log "Hello world!"))

(repl/connect "http://localhost:9000/repl")
