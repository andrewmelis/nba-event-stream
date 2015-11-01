(ns nba-harvester.core
  (:require [nba-harvester.play-by-play :as pbp]
            [nba-harvester.producer :as producer]
            [nba-harvester.scoreboard :as scoreboard]
            [nba-harvester.scheduler :as scheduler])
  (:gen-class))

(def a (atom 5))

(defn pbp-forever [team-abbreviation]
  (let [game-id (scoreboard/team->game-id team-abbreviation)]
    (while (pos? @a)
      (doall
       (map #(producer/publish-nba-pbp-event team-abbreviation %)
            (pbp/new-play-by-play-events game-id)))
       (Thread/sleep 5000)
       (swap! a dec)
       (println @a))))

(defn -main
  "I don't do a whole lot ... yet."
  [team-abbreviation]
  (pbp-forever team-abbreviation))

