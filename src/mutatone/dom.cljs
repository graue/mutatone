(ns mutatone.dom
  (:require [dommy.utils :as utils]
            [dommy.core :as dommy]
            [dommy.attrs :refer [attr set-attr!]]
            [goog.events :as events]
            [mutatone.theory :refer [phrase->str scalify]])
  (:require-macros [dommy.macros :refer [node sel sel1 deftemplate]]))

(deftemplate melody-template [melody idx]
  [:span
    [:button {:class "play-btn" :data-idx idx} "Play"]
    [:button {:class "breed-btn" :data-idx idx :id (str "breed-btn-" idx)}
             "Breed"]
    (str (:root melody) " " (:scale melody) ": "
         (apply str
                (interpose ", " (map str (:intervals melody)))))])

(deftemplate main-ui [melodies]
  [:div#melodies
    [:ul
      (for [i (range (count melodies))]
        [:li (melody-template (nth melodies i) i)])
    [:p "Then some more stuff"]]])

(defn render-melodies [melodies play-cb breed-cb]
  (dommy/replace! (sel1 :#melodies)
                  (main-ui melodies))
  (doseq [btn (sel :.play-btn)]
    (events/listen btn "click" #(play-cb (attr btn "data-idx"))))
  (doseq [btn (sel :.breed-btn)]
    (events/listen btn "click" #(breed-cb (attr btn "data-idx")))))

(defn disable-breed-button [idx]
  (set-attr! (sel1 (str "#breed-btn-" idx)) :disabled))
