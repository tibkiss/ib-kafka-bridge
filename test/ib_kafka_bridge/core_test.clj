(ns ib-kafka-bridge.core-test
  (:use midje.sweet)
  (:require [ib-kafka-bridge.core :refer :all]
            [kafka-clj.client :as kafka]
            [clojure.data.json :as json]
            [ib-re-actor.gateway :as ib-gw]))


(fact "Contract creation works"
      (create-contract "SPY") => {:symbol "SPY" :type :equity :exchange "SMART" :currency "USD"}
      (create-contract "HDP") => {:symbol "HDP" :type :equity :exchange "SMART" :currency "USD"}
      (create-contract "USD.HUF") => {:symbol "USD" :type :cash :exchange "IDEALPRO" :currency "HUF"}
      (create-contract "EUR.USD") => {:symbol "EUR" :type :cash :exchange "IDEALPRO" :currency "USD"}
      )

(fact "Market Data Handler works"
      (#'ib-kafka-bridge.core/create-market-data-handlers ..kafka-conn.. ..contract..) =>
      (just {:data anything :end anything :error anything}))

(fact "Dispatch to Kafka works"
      (let [handlers (#'ib-kafka-bridge.core/create-market-data-handlers ..kafka-conn.. ..contract..)
            data-handler (:data handlers)
            test-json (str "Blah")]
        (data-handler ..test-msg..) => irrelevant
        (provided (json/write-str ..test-msg..) => test-json,
                  (kafka/send-msg ..kafka-conn.. ..contract.. anything) => nil)
        )
      )

(fact "Gateway creation works"
      (let [security ["EUR.USD" "DATA" "HDP"]]
        (create-gw ..kafka-uri.. ..tws-uri.. security) => irrelevant
        (provided (create-tws-connection ..tws-uri..) => ..tws-conn..,
                  (create-kafka-connection ..kafka-uri..) => ..kafka-conn..,
                  (ib-gw/request-market-data ..tws-conn.. anything anything false anything) => nil :times 3)
        ))