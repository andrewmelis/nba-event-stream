(ns nba-twitter-pbp.core
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :refer [chan pipe]]
            [nba-twitter-pbp.components.logger :as logger]
            [nba-twitter-pbp.components.twitter :as twitter]
            [nba-twitter-pbp.components.tester :as tester])
  (:gen-class))


(def twitter-creds
  {:app-key "2Jvybh2lTuNDzM7aWR8Irn5Jc"
   :app-secret "LVETkiGlbOn1uD7arAxxbdgBbomEydiSymTYaZFEmVuQTHHR8C"
   :user-token "4767675388-qpAZi1hPl4Goe9SzdlluSTVsoLRTKBYD4tCdNkZ"
   :user-token-secret "KcKWOVX3hz5bKWEgJveQkgUw4yCwSpxKI5kztsxGRGAId"})

(defn system []
  (let [output-chan (chan)
        input-chan (chan (map clojure.string/upper-case))]
    (pipe input-chan output-chan)
    
    (component/system-map
     ;; :logger (logger/new-logger output-chan)
     :twitter (twitter/new-twitter-publisher twitter-creds output-chan)
     ;; :tester (tester/new-tester input-chan)
     :exposed input-chan)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
