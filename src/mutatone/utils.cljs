(ns mutatone.utils)

(defn flat-str [& args]
  (apply str (flatten args)))
