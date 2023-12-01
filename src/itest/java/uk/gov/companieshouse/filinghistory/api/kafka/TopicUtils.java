package uk.gov.companieshouse.filinghistory.api.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

final class TopicUtils {

    static final String MAIN_TOPIC = "filing-history-delta";
    static final String RETRY_TOPIC = "filing-history-delta-filing-history-delta-consumer-retry";
    static final String ERROR_TOPIC = "filing-history-delta-filing-history-delta-consumer-error";
    static final String INVALID_TOPIC = "filing-history-delta-filing-history-delta-consumer-invalid";

    private TopicUtils() {
    }

    static int countTopicRecords(ConsumerRecords<?, ?> records, String topic) {
        int count = 0;
        for (ConsumerRecord<?, ?> ignored : records.records(topic)) {
            count++;
        }
        return count;
    }
}
