(ns mutatone.utils)

(defn flat-str [& args]
  (apply str (flatten args)))

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
