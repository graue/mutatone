(ns mutatone.dom
  (:require [dommy.utils :as utils]
            [dommy.core :as dommy]
            [mutatone.theory :refer [phrase->str scalify]])
  (:require-macros [dommy.macros :refer [node sel sel1 deftemplate]]))

(deftemplate melody-template [melody idx]
  [:span
    [:button {:class "play-btn" :data-idx idx} "Play"]
    [:button {:class "breed-btn" :data-idx idx} "Breed"]
    (phrase->str (scalify (:intervals melody)
                          (:scale melody)
                          (:root melody)))])

(deftemplate main-ui [melodies]
  [:div
    [:ul
      (for [i (range (count melodies))]
        [:li (melody-template (nth melodies i) i)])
    [:p "Then some more stuff"]]])

(defn render-melodies [melodies]
  (dommy/replace! (sel1 :#melodies)
                  (main-ui melodies)))
