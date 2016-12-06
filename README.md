# ib-kafka-bridge

Interactive Brokers API to Kafka bridge.

[![Build Status](https://api.travis-ci.org/tibkiss/ib-kafka-bridge.svg?branch=master)](https://travis-ci.org/tibkiss/ib-kafka-bridge)

## Usage

```lein run --kafka-uri localhost:9092 --tws-uri localhost:7500:117 SPY EUR.USD```

## Prerequisites

 - Trader Workstation (version 951 and above)
 - Compiled Interactive Brokers API ([build script](https://github.com/tibkiss/ib-kafka-bridge/blob/master/build-tools/01-install-twsapi.sh))
 - JSAB's [ib-re-actor](https://github.com/jsab/ib-re-actor) ([build script](https://github.com/tibkiss/ib-kafka-bridge/blob/master/build-tools/02-install-ib-re-actor.sh))
 - Kafka
 - Leiningen
 - JVM

## License

Copyright © 2016 Tibor Kiss

Distributed under [Apache License Version 2.0](http://www.apache.org/licenses/).
