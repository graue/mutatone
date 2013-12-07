(ns mutatone.dom
  (:require [dommy.utils :as utils]
            [dommy.core :as dommy])
  (:require-macros [dommy.macros :refer [node sel sel1]]))

(defn render-melodies [melodies]
  (dommy/replace! (sel1 :#melodies)
                   (node [p "This is a thing, cookies!!"])))
