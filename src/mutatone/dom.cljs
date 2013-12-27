(ns mutatone.dom
  (:require [om.core :as om :include-macros true]
            [sablono.core :refer [html] :include-macros true]
            [goog.events :as events]
            [cljs.core.async :refer [put!]]
            [mutatone.theory :refer [phrase->str scalify]]
            [mutatone.utils :refer [flat-str]]))

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
