(ns nba-harvester.scoreboard
  (:require [clj-http.client :as client]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.local :as l]
            [cheshire.core :as json]
            [net.cgrand.enlive-html :as html]))

(defn base-url []
  "http://stats.nba.com/stats")

(defn scoreboard-endpoint []
  "/scoreboard")

(defn last-game-day []
  (t/date-time 2015 10 23))

(defn request-formatted-date
  ([] (f/unparse (f/formatter "MM/dd/yyyy") (t/now)))
  ([date] (f/unparse (f/formatter "MM/dd/yyyy") date)))

(defn scoreboard-params [& {:keys [game-date league-id day-offest]
                            :or {game-date (request-formatted-date)
                                 league-id "00"
                                 day-offest "0"}}]
  (let [day-offset-str (str "DayOffset=" day-offest)
        league-id-str (str "LeagueID=" league-id)
        game-date-str (str "gameDate=" game-date)]
    (clojure.string/join "&" [day-offset-str league-id-str game-date-str])))

(defn scoreboard-request []
  (str (base-url) (scoreboard-endpoint) "?" (scoreboard-params :game-date (request-formatted-date (last-game-day)))))

(defn scores []
  (-> (client/get (scoreboard-request))
      :body
      (json/parse-string true)))

(defn available-games []
  (filter #(= "Available" (:name %)) (:resultSets (scores))))

(defn nba-headers->keywords [headers]
  (->> headers
       (map clojure.string/lower-case)
       (map #(clojure.string/replace % #"_" "-" ))
       (map keyword)))

(defn parse-gameheaders [scoreboard-response]
  (let [game-headers (first (filter #(= "GameHeader" (:name %)) (:resultSets scoreboard-response)))
        {:keys [headers rowSet]} game-headers
        headers (nba-headers->keywords headers)]
    (map #(zipmap headers %) rowSet)))

(defn game-record-for-team
  "return game-id for a given team"
  [game-headers team-abbreviation]
  (first
   (filter (fn [game-record]
             (let [{:keys [gamecode game-id]} game-record]
               (.contains gamecode team-abbreviation))) game-headers)))

(defn example-play-by-play []
  (client/get "http://stats.nba.com/stats/playbyplay?gameID=0011500104&StartPeriod=0&EndPeriod=0"))


(defn parse-play-by-play [play-by-play-response]
  (let [parsed-response (-> play-by-play-response
                            (:body)
                            (json/parse-string true)
                            (:resultSets))]
        ;; {:keys [headers rowSet]} parsed-response
        ;; headers (nba-headers->keywords headers)]
    parsed-response))

    ;; (map #(zipmap headers %) rowSet)))
