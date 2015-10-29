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

(defn request-formatted-date
  ([] (request-formatted-date (t/today)))
  ([date] (f/unparse-local-date (f/formatter "MM/dd/yyyy") date)))

(defn scoreboard-params [& {:keys [game-date league-id day-offest]
                            :or {game-date (request-formatted-date)
                                 league-id "00"
                                 day-offest "0"}}]
  (let [day-offset-str (str "DayOffset=" day-offest)
        league-id-str (str "LeagueID=" league-id)
        game-date-str (str "gameDate=" game-date)]
    (clojure.string/join "&" [day-offset-str league-id-str game-date-str])))

(defn scoreboard-request []
  (str (base-url) (scoreboard-endpoint) "?" (scoreboard-params :game-date (request-formatted-date))))

(defn scores []
  (-> (client/get (scoreboard-request))
      :body
      (json/parse-string true)))

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
  (client/get "http://stats.nba.com/stats/playbyplay?gameID=0021500016&StartPeriod=0&EndPeriod=0"))

(defn next-try []
  (client/get "http://data.nba.com/data/10s/html/nbacom/2015/gameinfo/20151028/0021500017_playbyplay_csi.html"))

"http://data.nba.com/data/10s/html/nbacom/2015/gameinfo/20151028/0021500017_playbyplay_csi.html"

(defn parse-play-by-play [play-by-play-response]
  (let [parsed-response (-> play-by-play-response
                            (:body)
                            (json/parse-string true)
                            (:resultSets)
                            (first))
        {:keys [headers rowSet]} parsed-response
        headers (nba-headers->keywords headers)]
    (map #(zipmap headers %) rowSet)))


(defn play-by-play []
  (client/get "http://data.nba.com/5s/json/cms/noseason/scoreboard/20151028/games.json"))

(defn parse-play-by-play [raw-play-by-play]
  (-> raw-play-by-play
      (:body)
      (json/parse-string true)
      (:sports_content)
      (:games)
      (:game)))
