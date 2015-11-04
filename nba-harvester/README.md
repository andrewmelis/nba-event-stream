# nba-harvester

Creates a play by play event stream for the NBA team passed in as a command line argument. Expects a team abbreviation, like CLE for the Cleveland Cavaliers or LAL for the Los Angeles Lakers.

The application sends out Apache Kafka events (currently hard-coded to localhost, but configurable in the future).

## Start me up

```
# first terminal window
$ cd /path/to/kafka-v-x-y-z
$ bin/zookeeper-server-start.sh config/zookeeper.properties 

# second terminal window
$ cd /path/to/kafka-v-x-y-z
$ bin/kafka-server-start.sh config/server.properties

# third terminal window
$ cd /path/to/nba-harvester
$ lein run CLE

# look at the events being sent in the console
$ cd /path/to/kafka-v-x-y-z
$ bin/kafka-console-consumer.sh --zookeeper localhost:2181 --from-beginning --topic CLE
# more producers coming soon...
```

## TODOs
- configurable settings (kafka endpoints, loop cycle time, other env type stuff)
- not super robust right now. will blow up if pretty much anything unexpected happens
  - blows up if you pass a team name that does not play today
  - blows up if clock crosses midnight and you're trying to follow a game
- better parsing of the responses from the stats server. right now the app sends an ugly string for each event rather than creating a true event map from the nba stats play-by-play endpoint. for example, extracting which team performed each event is definitely doable.
- given challenges obtaining play-by-play data (most sites want you to pay for play-by-play, and the `stats.nba.com/playbyplay` endpoint returns empty until after each game is over), the application uses a url from the nba that returns html intended to be displayed in the browser. the diffing / parsing logic could probably be better, and possibly extracted into another service.
- in that diffing logic, the `atom` approach could be improved. it can only work with one game at a time -- something as simple as a map with key-value pairs for game-ids and event lists could improve the situation, but there's probably a better way
- more consumers!
  - post play-by-play to twitter
  - build a "speed-layer" with the event stream. POC could be something like aggregating a box score with the events, and the batch layer could call the nba's box score and endpoints and merge appropriately
  - grade the hand-rolled box score's accuracy as well
  - make some sweet graphs/dashboards visualizing live game data
- publish event streams for ALL of each day's games
- automate the scheduling of these event streams so that the app doesn't just poll on a loop in the middle of the night when there are no games
- write some tests 
- deploy somewhere and let the thing run

# Stretch TODOs
- be employed by the NBA to make this
