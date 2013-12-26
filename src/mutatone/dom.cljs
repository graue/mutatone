(ns mutatone.dom
  (:require [om.core :as om :include-macros true]
            [sablono.core :refer [html] :include-macros true]
            [dommy.utils :as utils]
            [dommy.core :as dommy]
            [dommy.attrs :refer [attr set-attr!]]
            [goog.events :as events]
            [cljs.core.async :refer [put!]]
            [mutatone.theory :refer [phrase->str scalify]]
            [mutatone.utils :refer [flat-str]])
  (:require-macros [dommy.macros :refer [node sel sel1 deftemplate]]))

(defn melody-widget
  [{:keys [id root scale intervals will-breed] :as melody}
   {:keys [comm]}]
  (om/component
    (html [:span
            [:button {:className "play-btn"
                      :onClick #(put! comm [:play melody])}
             "Play"]
            [:button {:className "breed-btn"
                      :disabled (if will-breed "true" "")
                      :onClick #(put! comm [:breed melody])}
                     "Breed"]
            (flat-str root " " scale ": "
                      (interpose ", " intervals))])))

(defn melody-list-widget
  [melodies {:keys [comm]}]
  (om/component
    (html
      [:ul
        (map (fn [idx]
               [:li (om/build melody-widget melodies
                              {:opts {:comm comm}, :path [idx]})])
             (range (count melodies)))])))

(deftemplate melody-template [melody idx]
  [:span
    [:button {:class "play-btn" :data-idx idx} "Play"]
    [:button {:class "breed-btn" :data-idx idx :id (str "breed-btn-" idx)}
             "Breed"]
    (flat-str (:root melody) " " (:scale melody) ": "
              (interpose ", " (:intervals melody)))])

(deftemplate main-ui [melodies]
  [:div#melodies
    [:ul
      (for [i (range (count melodies))]
        [:li (melody-template (nth melodies i) i)])]])

(defn render-melodies [melodies play-cb breed-cb]
  (dommy/replace! (sel1 :#melodies)
                  (main-ui melodies))
  (doseq [btn (sel :.play-btn)]
    (events/listen btn "click" #(play-cb (attr btn "data-idx"))))
  (doseq [btn (sel :.breed-btn)]
    (events/listen btn "click" #(breed-cb (attr btn "data-idx")))))

(defn disable-breed-button [idx]
  (set-attr! (sel1 (str "#breed-btn-" idx)) :disabled))
