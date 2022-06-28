# ring-sink

## Running

`git clone https://github.com/rotaliator/ring-sink`

`clojure -X ring-sink.main/-main`

or

`clojure -m ring-sink.main`

## building jar

`clojure -T:build uberjar`


## building docker image

`clojure -T:jib`

## running from docker image

`docker run --rm -p 3000:3000 rotaliator/ring-sink`
