(ns nba-twitter-pbp.logger
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :refer [chan <!! >!! go go-loop <! >! pipeline]]))

(defn async-tester
  [chan msg]
  (go
    (dotimes [n 100]
      (let [msg (or msg "happy Clojure bug awesome cool ")]
        (>! chan (str msg n))))))

(defn process-messages
  "for now, prints out messages. nothing special. `status` is an atom"
  [status msg-chan]
  (go-loop []
    (when-some [val (<! msg-chan)]
      (println "received: " val)
      (when (= :running @status) (recur)))))

(defrecord Logger [status msg-chan]
  component/Lifecycle
  (start [component]
    (reset! (:status component) :running)
    (process-messages status msg-chan)
    component)
  (stop [component]
    (reset! (:status component) :stopped)
    component))

(defn new-logger [msg-chan]
  (->Logger (atom :init) msg-chan))
