(ns nba-harvester.scheduler
  (:require [clj-time.core :as t]
            [clj-time.periodic :refer [periodic-seq]]
            [chime :refer [chime-at]]))

(defn ten-second-intervals []
  (periodic-seq (t/now) (t/seconds 10)))

(defn schedule-every-10-seconds [task]
  (chime-at (ten-second-intervals)
            task
            {:error-handler (fn [e]
                              (println e)
                              )}))
