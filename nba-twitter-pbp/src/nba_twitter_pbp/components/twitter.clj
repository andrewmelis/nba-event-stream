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

(defn publish-messages
  "creates new status update for every message received"
  [creds status msg-chan]
  (go-loop []
    (when-some [val (<! msg-chan)]
      (println "twitter received: " val)
      (tweet creds val)
      (when (= :running @status) (recur))))) ; off-by-one error here. doesn't check again when it receives a message

(defrecord TwitterPublisher [twitter-creds status msg-chan]
  component/Lifecycle
  (start [component]
    (if (not= :running @status)
      (let [creds (make-twitter-creds twitter-creds)]
        (reset! (:status component) :running)
        (publish-messages creds status msg-chan)
        (assoc component :creds creds))
      component))
  (stop [component]
    (if (= :running @status)
      (do (reset! (:status component) :stopped)
          component)
      component)))

(defn new-twitter-publisher [twitter-config msg-chan]
  (->TwitterPublisher twitter-config (atom :init) msg-chan))
