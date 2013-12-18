(ns mutatone.theory
  "'Music theory', or in other words, utility functions that have nothing to
  do with audio output (they only manipulate values)."
  (:require [clojure.set :as cset]))

(defn midi->hz
  "Convert a MIDI note number to its frequency in Hz."
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
  {"C"  0
   "C#" 1
   "D"  2
   "D#" 3
   "E"  4
   "F"  5
   "F#" 6
   "G"  7
   "G#" 8
   "A"  9
   "A#" 10
   "B"  11})

(def notenum->name
  (cset/map-invert notes))

(defn midi->str
  "Convert a MIDI note number to a string representation.
  Note: by convention, A4 = 440 Hz = 69."
  [note-num]
  (let [num-in-octave (mod note-num 12)
        octave (dec (Math/floor (/ note-num 12)))]
    (str (notenum->name num-in-octave) octave)))

(defn phrase->str
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
      (if (pos? intervals)
        (sum (take intervals scale))

        ; Negative case, e.g. for -2, sum the *last* two intervals in the
        ; scale definition and negate the result.
        (- (sum (take (- intervals) neg-scale))))

      ; If passed a collection, map this function across each element.
      (map #(dia->chrom % scale-name) intervals))))

(defn scalify
  "Convert a list of intervals to MIDI note numbers based on the scale
  named `scale` starting on `note` in octave `octave`."
  [intervals scale note & [octave]]
  (let [octave (or octave 4)
        octave-offset (* 12 (inc octave))
        note-offset (notes (.toUpperCase note))
        chromatic-intervals (dia->chrom intervals scale)]
    (map (partial + octave-offset note-offset) chromatic-intervals)))

(defn phrase->notes
  "Converts a phrase specified as a seq of MIDI note numbers into a seq of
  hashes with a :start and :freq. `length` is the time between notes."
  [phrase length start-time]
  (remove nil?
          (for [i (range (count phrase))]
            (let [note (nth phrase i)
                  start (+ start-time (* i length))]
              (when note  ; Ignore nils, treating them as rests.
                {:freq (midi->hz note)
                 :start start})))))
