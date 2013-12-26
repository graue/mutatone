(ns mutatone.core
  (:require [mutatone.theory :as t]
            [mutatone.audio :as a]
            [hum.core :as hum]
            [mutatone.dom :as dom]
            [cljs.core.async :refer [<! chan]]
            [om.core :as om :include-macros true]
            [mutatone.melodies :as m]
            [mutatone.utils :refer [with-id]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def ctx (atom nil))
(def osc (atom nil))
(def filt (atom nil))
(def gain (atom nil))

(defn audio-inited? []
  (not (nil? @ctx)))

(defn init-audio []
  (when (audio-inited?)
    (throw "Audio already initialized"))
  (reset! ctx (hum/create-context))
  (reset! osc (hum/create-osc @ctx :triangle))
  (set! (.-type @osc) "triangle")  ; Otherwise we get a sine in Firefox...?
  (reset! filt (hum/create-biquad-filter @ctx))
  (reset! gain (hum/create-gain @ctx))
  (hum/connect @osc @filt)
  (hum/connect @filt @gain)
  (hum/start-osc @ctx @osc)
  (hum/connect-output @ctx @gain)
  nil)

(defn panic []
  (a/kill-notes @osc @gain))

(def app-state
  (atom
    (with-id {:melodies (vec (map with-id m/seed-melodies))})))

(defn breed-new-batch [p1 p2]
  "Breeds a new batch of melodies from the given parents."
  (vec (for [_ (range 6)]
         (m/breed-from p1 p2))))

(defn play [melody]
  (when-not (audio-inited?)
    (init-audio))
  (panic)  ; Kill any notes currently playing.
  (let [raw-notes (t/scalify (:intervals melody) (:scale melody)
                             (:root melody) 4)]
    (a/play-phrase @osc @gain raw-notes 0.75)))

(defmulti handle-event
  (fn [_ [ev-type _]] ev-type))

(defmethod handle-event :play [app [_ melody]]
  (play melody))

(defmethod handle-event :breed [app [_ melody]]
  (om/update! melody #(assoc % :will-breed true))
  (let [breeders (filter :will-breed (om/get-state app [:melodies]))]
    (.log js/console "The breeders are:" (str breeders))
    (when (= (count breeders) 2)
      (om/update! app [:melodies]
                  (->> breeders
                       (apply breed-new-batch)
                       (map with-id)
                       vec
                       constantly)))))

(defmethod handle-event :default [_ [ev-type id]]
  (.log js/console "An event of unknown type" (name ev-type) "occurred,"
        "respecting element with id" id))  ; FIXME

(defn app-methods [{:keys [melodies] :as app}]
  (reify
    om/IWillMount
    (will-mount [_ owner]
      (let [comm (chan)]
        (om/set-state! owner [:comm] comm)
        (go (while true
              (handle-event app (<! comm))))))

    om/IRender
    (render [_ owner]
      (om/build dom/melody-list-widget app
                {:opts {:comm (om/get-state owner [:comm])}
                 :path [:melodies]}))))

(om/root app-state app-methods (.getElementById js/document "melodies"))
