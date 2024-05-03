package uk.gov.companieshouse.filinghistory.consumer.kafka;

import java.time.Duration;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

final class KafkaUtils {

    static final String MAIN_TOPIC = "filing-history-delta";
    static final String RETRY_TOPIC = "filing-history-delta-filing-history-delta-consumer-retry";
    static final String ERROR_TOPIC = "filing-history-delta-filing-history-delta-consumer-error";
    static final String INVALID_TOPIC = "filing-history-delta-filing-history-delta-consumer-invalid";

    private KafkaUtils() {
    }

    static int noOfRecordsForTopic(ConsumerRecords<?, ?> records, String topic) {
        int count = 0;
        for (ConsumerRecord<?, ?> ignored : records.records(topic)) {
            count++;
        }
        return count;
    }

    static Duration kafkaPollingDuration() {
        String kafkaPollingDuration = System.getenv().containsKey("KAFKA_POLLING_DURATION") ?
                System.getenv("KAFKA_POLLING_DURATION") : "1000";
        return Duration.ofMillis(Long.parseLong(kafkaPollingDuration));
    }
}
