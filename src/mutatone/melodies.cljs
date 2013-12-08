(ns mutatone.melodies
  (:require [mutatone.theory :as t]))

;;; Melodies and breeding.
(def seed-melodies
  [{:intervals [0 0 1 1 3 2 -1 -1 0 0 1 1 3 2 -1 -1]
    :scale "phrygian"
    :root "B"}

   {:intervals [0 0 -1 -1 -2 -2 -3 -3 0 0 2 2 1 1 1 1]
    :scale "minor pentatonic"
    :root "C"}

   {:intervals [0 1 2 1 4 5 3 0 2 2 -1 -2 -1 -1 0 0 0 0 0 0]
    :scale "major"
    :root "D#"}])

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

(defn random-from [seq]
  (let [idx (rand-int (count seq))]
    (nth seq idx)))

(defn possibly-mutate-number [x]
  (let [make-negative? (= (rand-int 2) 0)
        sign (if make-negative? -1 1)
        sign (if (= (rand-int 3) 0) sign 0)]
    (+ x (* sign (Math/round (- 2 (log-random-between 1 2)))))))

(defn possibly-mutate-intervals [intervals]
  (map possibly-mutate-number intervals))

(defn mix-vectors [v1 v2]
  (loop [v1 v1, v2 v2, acc []]
    (if (= (rand-int 4) 0)
      (recur v2 v1 acc)
      (if (empty? v1)
        acc
        (recur (rest v1) (rest v2) (conj acc (first v1)))))))

(defn breed-from [m1 m2]
  {:intervals (possibly-mutate-intervals
                (mix-vectors (:intervals m1) (:intervals m2)))
   :scale (if (= 0 (rand-int 5))
            (random-from (keys t/scales))
            (if (= 0 (rand-int 2))
              (:scale m1)
              (:scale m2)))
   :root (if (= 0 (rand-int 5))
           (random-from (keys t/notes))
           (if (= 0 (rand-int 2))
            (:root m1)
            (:root m2)))})
