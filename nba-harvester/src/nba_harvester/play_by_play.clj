(ns nba-harvester.play-by-play
  (:require [clojure.data :as data]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.local :as l]
            [cheshire.core :as json]
            [net.cgrand.enlive-html :as html]))

;; url->html-structure
(defn- fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn- game-id->play-by-play-url
  "assumes game is from today. CHANGE LATER"
  [game-id]
  (let [date-time (t/today)]
    (str "http://data.nba.com/data/10s/html/nbacom/"
         (f/unparse-local-date (f/formatters :year) date-time)
         "/gameinfo/"
         "20151101"
         ;; (f/unparse-local-date (f/formatters :basic-date) date-time)
         "/"
         game-id
         "_playbyplay_csi.html")))

;; eventually turn this into a hash
(defn- table-record->event-string [table-record]
  (->> table-record
       (map html/text)
       (map clojure.string/trim)
       (clojure.string/join " ")))

;; unfortunately, sends entire set of events for the game
;; diff them here, or make separate service for that?
(defn- play-by-play-for-game [game-id]
  (let [raw-content (fetch-url (game-id->play-by-play-url game-id))]
        (->> (html/select raw-content [:tr])
             (map :content)
             (map (fn [event]
                    (remove #(= String (class %)) event)))
             (map table-record->event-string))))
;; DIFF MACHINE

(def last-pbp (atom []))

;; (map (fn [event num]
;;        (assoc {}
;;               :content event
;;               :event-num num)) old-pbp (range))

(defn- raw-pbp->new-pbp-events
  "accepts a play-by-play list and compares to last saved pbp list
  saves input seq as side-effect"
  [new-pbp-xs]
  (let [new-events (->> (data/diff @last-pbp new-pbp-xs)
                        (second)
                        (partition-by #(nil? %)) ; HACK to avoid duplicates caused by updated pbp events. 
                        (last)
                        (vec))]
    (println "last-pbp: " @last-pbp)
    (println "new-events: " new-events)
    (swap! last-pbp concat new-events) ; note SIDE-EFFECT -- also, is concat bad?
    new-events))

(defn new-play-by-play-events
  "events are not repeated for different callers.
  could lead to missed events if called from multiple places
  also currently only works for one game"
  [game-id]
  (-> game-id
      (play-by-play-for-game)
      (raw-pbp->new-pbp-events)))
