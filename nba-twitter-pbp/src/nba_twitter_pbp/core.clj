(ns nba-twitter-pbp.core
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :refer [chan pipe]]
            [nba-twitter-pbp.logger :as logger])
  (:gen-class))


(defn system []
  (let [output-chan (chan 100)
        input-chan (chan 10 (map clojure.string/upper-case))]
    (pipe input-chan output-chan)
    
    (component/system-map
     :logger (logger/new-logger output-chan)
     :exposed-channel input-chan)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
