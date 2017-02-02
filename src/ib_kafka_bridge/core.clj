(ns ib-kafka-bridge.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json]
            [kafka-clj.client :as kafka]
            [ib-re-actor.gateway :as ib-gw]
            [ib-re-actor.synchronous :as ib-sync]
            [ib-re-actor.client-socket :as ib-cs]
            [clj-time.coerce :as coerce])
  (:import (java.io PrintWriter)))

(def POSITIONS-TOPIC "ib-positions")

(defn- wait-for
  "Invoke predicate every interval (default 10) seconds until it returns true,
   or timeout (default 150) seconds have elapsed. E.g.:
       (wait-for #(< (rand) 0.2) :interval 1 :timeout 10)
   Returns nil if the timeout elapses before the predicate becomes true, otherwise
   the value of the predicate on its last evaluation."
  [predicate & {:keys [interval timeout]
                :or {interval 10
                     timeout 150}}]
  (let [end-time (+ (System/currentTimeMillis) (* timeout 1000))]
    (loop []
      (if-let [result (predicate)]
        result
        (do
          (Thread/sleep (* interval 1000))
          (if (< (System/currentTimeMillis) end-time)
            (recur)))))))

(defn- parse-int [s]
  (Integer/parseInt (re-find #"\A-?\d+" s)))

(defn create-equity-contract [symbol]
  {:symbol symbol :type :equity :exchange "SMART" :currency "USD"})

(defn create-currency-contract [symbol]
  (let [[base-currency quote-currency] (string/split symbol #"\.")]
    {:symbol base-currency :type :cash :exchange "IDEALPRO" :currency quote-currency}))

(defn create-contract [symbol]
  (cond
    (re-find #"\." symbol) (create-currency-contract symbol)
    :else (create-equity-contract symbol)))

(defn- create-market-data-handlers [kafka-connection contract]
  {:data  (fn [msg]
            (log/info contract "-> " msg)
            (kafka/send-msg kafka-connection contract (.getBytes (json/write-str msg)))
            )

   :end   #(log/info "End of the request.")
   :error #(log/error "Error: " %)}
  )

(defn- create-positions-handlers [kafka-connection]
  {:data  (fn [msg]
            (log/info "Position: " msg)
            (kafka/send-msg kafka-connection POSITIONS-TOPIC (.getBytes (json/write-str msg)))
            )
   :end   #(log/info "End of positions request.")
   :error #(log/error "Error while processing positions: " %)
   }
  )

(defn create-tws-connection [tws-uri]
  (log/debug "Connecting to TWS: " tws-uri)
  (let [[tws-host tws-port client-id] (string/split tws-uri #":")
        tws (ib-gw/connect (parse-int client-id) tws-host (parse-int tws-port))]
    (wait-for #(ib-gw/is-connected? tws) :interval 1 :timeout 10)
    (log/debug "TWS Connected!")
    tws
    ))

(defn create-kafka-connection [kafka-uri]
  (log/debug "Connecting to Kafka: " kafka-uri)
  (let [[kafka-host kafka-port] (string/split kafka-uri #":")]
    (kafka/create-connector [{:host kafka-host :port (parse-int kafka-port)}]
                            {:flush-on-write true
                             :topic-auto-create true
                             :acks 0})))

(defn create-gw
  [kafka-uri tws-uri security]
  (log/debug "Bridging between" tws-uri " and " kafka-uri)
  (let [tws-conn (create-tws-connection tws-uri)
        kafka-conn (create-kafka-connection kafka-uri)
        contracts (map create-contract security)
        ticks ["236" "233"]]
    (log/debug "Subscribing to contracts: " contracts)
    (doall
      (map
        #(ib-gw/request-market-data tws-conn %1 ticks false (create-market-data-handlers kafka-conn %2))
        contracts security))
    (ib-gw/request-positions tws-conn (create-positions-handlers kafka-conn)))
  )

(def cli-options
  [
   ["-k" "--kafka-uri URI" "Kafka connection URI (host:port)"
    :default "localhost:9092"]
   ["-t" "--tws-uri URI" "Trader Workstation URI (host:port:client-id)"
    :default "localhost:7500:117"]
   ["-v" nil "Verbosity level"
    :id :verbosity
    :default 0
    :assoc-fn (fn [m k _] (update-in m [k] inc))]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["IB to Kafka Bridge"
        ""
        "Usage: ib-kafka-bridge [options] <security>..."
        ""
        "Options:"
        options-summary
        ""
        "Arguments:"
        "  <security>    Space delimited list of securities"
        ""
        "The specificied security (equity or currency-pair) will be mapped to a Kafka topic named after the security"
        "Example use: ib-kafka-bridge SPY EUR.USD"]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) (exit 0 (usage summary))
      (= (count arguments) 0) (exit 1 (usage summary))
      errors (exit 1 (error-msg errors)))
    (when (> (:verbosity options) 0) (println "Increase log level?"))
    ; Provide ability to serialize Joda DateTime's to JSON
    (extend-type org.joda.time.DateTime
      json/JSONWriter
      (-write [date out]
        (json/-write (coerce/to-string date) out)))
    (create-gw (:kafka-uri options) (:tws-uri options) arguments)
    (log/info "IB - Kafka bridge created. Press CTRL-c to exit.")
    (while true
      (Thread/sleep 2000))))
