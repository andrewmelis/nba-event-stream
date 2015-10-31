(ns nba-harvester.scoreboard
  (:require [clojure.data :as data]
            [clj-http.client :as client]
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

;; play by play stuff

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))


;; (defn game-id->play-by-play-url [game-id]
;;   (str "http://data.nba.com/data/10s/html/nbacom/"
;;        (current-year)
;;        "/gameinfo/"
;;        (date) ; no slashes this time
;;        "/"
;;        game-id
;;        "_playbyplay_csi.html"))

(def table-record
  '({:tag :td,
     :attrs {:class "nbaGIPbPLft"},
     :content (" ")}
    {:tag :td,
     :attrs {:class "nbaGIPbPMidScore"},
     :content ("10:56 " {:tag :br,
                         :attrs nil,
                         :content nil} "[HOU 3-1]\n            ")}
    {:tag :td,
     :attrs {:class "nbaGIPbPRgtScore"},
     :content (" Lawson Free Throw 1 of 2 (1 PTS) ")}))

;; eventually turn this into a hash
(defn table-record->event-string [table-record]
  (->> table-record
       (map html/text)
       (map clojure.string/trim)



;; WIP
;; (let [raw-content (fetch-url play-by-play-url)]
;;                             (->> (html/select raw-content [:tr])
;;                                  (map :content)
;;                                  (map (fn [event]
;;                                         (remove #(= String (class %)) event)))
;;                                  (take 15)
;;                                  (last)))
(defn play-by-play-for-game [game-id]
  (let [raw-content (fetch-url (game-id->play-by-play-url game-id))]
        (->> (html/select raw-content [:tr])
             (map :content)
             (map (fn [event]
                    (remove #(= String (class %)) event)))
             (map table-record->event-string))))
                  ;; (drop 1) ; drop teams? shouldn't i know the opponents by now?
             
             

             ;; (clojure.string/trim (html/text (second (last (take 10 (let [raw-content (fetch-url play-by-play-url)]
             ;;          (->> (html/select raw-content [:tr])
             ;;               (map :content)
             ;;               (map (fn [event]
             ;;                      (remove #(= String (class %)) event))))))))))
(def play-by-play-url "http://data.nba.com/data/10s/html/nbacom/2015/gameinfo/20151030/0021500030_playbyplay_csi.html")


;; how to parse the table

;; (remove #(= String (class %)) (-> (fetch-url play-by-play-url)
;;                                   (html/select [:tr])
;;                                   (:content)))

;; ;; diff for two snapshots
;; (->> (clojure.data/diff old-pbp new-pbp)
;;      (second) ; get things "only-in-b"
;;      (remove nil?)

(defn parse-play-by-play [play-by-play-response]
  (let [parsed-response (-> play-by-play-response
                            (:body)
                            (json/parse-string true)
                            (:resultSets)
                            (first))
        {:keys [headers rowSet]} parsed-response
        headers (nba-headers->keywords headers)]
    (map #(zipmap headers %) rowSet)))

;; (defn raw-play-by-play []
;;   (client/get "http://data.nba.com/5s/json/cms/noseason/scoreboard/20151028/games.json"))

;; (defn parse-play-by-play [raw-play-by-play]
;;   (-> raw-play-by-play
;;       (:body)
;;       (json/parse-string true)
;;       (:sports_content)
;;       (:games)
;;       (:game)))
