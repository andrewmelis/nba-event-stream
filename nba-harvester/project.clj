(defproject nba-harvester "0.1.0"
  :description "produces an NBA event stream"
  :url "https://github.com/andrewmelis/nba-event-stream"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [clj-kafka "0.3.2"]
                 [clj-http "2.0.0"]
                 [clj-time "0.11.0"]
                 [cheshire "5.5.0"]
                 [enlive "1.1.6"]
                 [jarohen/chime "0.1.6"]]
  :main nba-harvester.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
