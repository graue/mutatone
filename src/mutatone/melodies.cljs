(ns mutatone.melodies
  "Melodies and breeding."
  (:require [mutatone.theory :as t]
            [mutatone.utils :refer [random-between log-random-between]]))

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
            (rand-nth (keys t/scales))
            (if (= 0 (rand-int 2))
              (:scale m1)
              (:scale m2)))
   :root (if (= 0 (rand-int 5))
           (rand-nth (keys t/notes))
           (if (= 0 (rand-int 2))
            (:root m1)
            (:root m2)))})
