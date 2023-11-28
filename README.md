# filing-history-delta-consumer
filing-history-delta-consumer is a Java service which utilises Spring Kafka to process filing-history deltas. It consumes the deltas from the filing-history-delta Kafka topic and transforms them before sending requests to filing-history-data-api for the delta to be persisted.
