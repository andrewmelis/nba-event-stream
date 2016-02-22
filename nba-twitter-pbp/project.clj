(defproject nba-twitter-pbp "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.2.374"]
                 [twitter-api "0.7.8"]
                 [com.stuartsierra/component "0.3.1"]]
  :main ^:skip-aot nba-twitter-pbp.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
