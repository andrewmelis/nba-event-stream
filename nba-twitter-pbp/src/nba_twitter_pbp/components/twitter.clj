(ns nba-twitter-pbp.components.twitter
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :refer [chan <!! >!! go go-loop <! >! pipeline]]
            [twitter.oauth :refer :all]
            [twitter.api.restful :refer :all]))

(defn make-twitter-creds [m]
  (let [{:keys [app-key app-secret user-token user-token-secret]} m]
   (make-oauth-creds app-key
                     app-secret
                     user-token
                     user-token-secret)))

(defn delete-tweet [creds tweet-id]
  (statuses-destroy-id :oauth-creds creds
                       :params {:id tweet-id}))

(defn tweet [creds msg]
  (statuses-update :oauth-creds creds
                   :params {:status msg}))

(defn publish-once
  [creds chan]
  (go
    (let [val (<! chan)]
      (tweet creds val))))

(defn publish-until-done
  [creds chan go]
  (go-loop []
    (when-some [val (<! chan)]
      (tweet creds val))
    (recur)))

(defn publish-messages
  "creates new status update for every message received"
  [creds status msg-chan]
  (go-loop []
    (when-some [val (<! msg-chan)]
      (tweet creds val))
    (when (= :running @status) (recur))))

(defrecord TwitterPublisher [twitter-creds status msg-chan]
  component/Lifecycle
  (start [component]
    (let [creds (make-twitter-creds twitter-creds)]
      (reset! (:status component) :running)
      ;; (publish-messages twitter-creds status msg-chan)
      ;; (publish-until-done twitter-creds msg-chan true)
      (publish-once twitter-creds msg-chan)
      component))
  (stop [component]
    (reset! (:status component) :stopped)
    component))

(defn new-twitter-publisher [twitter-config msg-chan]
  (->TwitterPublisher twitter-config (atom :init) msg-chan))
