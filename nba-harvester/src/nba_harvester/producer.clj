(ns nba-harvester.producer
  (:require [clj-kafka.new.producer :refer :all]))

(defn nba-producer [message-body]
  (with-open [pr (producer {"bootstrap.servers" "localhost:9092"} (byte-array-serializer) (byte-array-serializer))]
    (send pr (record "test-topic" (.getBytes message-body)))))
