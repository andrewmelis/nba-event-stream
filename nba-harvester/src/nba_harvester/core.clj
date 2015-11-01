(ns nba-harvester.core
  (:require [nba-harvester.play-by-play :as pbp]
            [nba-harvester.producer :as producer]
            [nba-harvester.scoreboard :as scoreboard]
            [nba-harvester.scheduler :as scheduler])
  (:gen-class))

(defn pbp-forever [team-abbreviation]
  (let [game-id (scoreboard/team->game-id team-abbreviation)]
    (doall
     (map #(producer/publish-nba-pbp-event team-abbreviation %)
          (pbp/new-play-by-play-events game-id)))))

(defn -main
  "I don't do a whole lot ... yet."
  [team-abbreviation]
  (println "Hello, World!"))
