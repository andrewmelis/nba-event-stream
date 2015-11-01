(ns nba-harvester.producer
  (:require [clj-kafka.new.producer :refer :all]))

(defn publish-nba-pbp-event [team-abbreviation message-body]
  (with-open [pr (producer {"bootstrap.servers" "localhost:9092"} (byte-array-serializer) (byte-array-serializer))]
    (send pr (record team-abbreviation (.getBytes message-body)))))
