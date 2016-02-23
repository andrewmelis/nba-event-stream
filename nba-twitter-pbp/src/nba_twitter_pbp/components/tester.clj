(ns nba-twitter-pbp.components.tester
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :refer [chan <!! >!! go go-loop <! >! pipeline]]))

(defn send-messages
  [status chan]
  (go
    (dotimes [n 2]
      (when (= :running @status)
        (let [time (java.util.Date.)]
          (>! chan (str n " the time is " time)))))
    (reset! status :stopped)))

(defrecord Tester [status msg-chan]
  component/Lifecycle
  (start [component]
    (reset! (:status component) :running)
    (send-messages status msg-chan)
    component)
  (stop [component]
    (reset! (:status component) :stopped)
    component))

(defn new-tester [msg-chan]
  (->Tester (atom :init) msg-chan))
