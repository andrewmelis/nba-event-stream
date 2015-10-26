(ns nba-harvester.core
  (:require [clj-http.client :as client]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [cheshire.core :as json])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
