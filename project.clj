(defproject ib-kafka-bridge "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://github.com/tibkiss/ib-kafka-bridge"
  :license {:name "Apache License Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :aot :all
  :main ib-kafka-bridge.core
  :resource-paths ["lib/twsapi-971.01-compiled.jar"]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/data.json "0.2.6"]
                 [ch.qos.logback/logback-classic "1.1.3"]
                 [kafka-clj "3.6.8"]
                 [ib-re-actor "0.1.8"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]]
                   :plugins [[lein-midje "3.0.0"]]}})
