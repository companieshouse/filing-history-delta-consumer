package uk.gov.companieshouse.filinghistory.consumer.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

public final class TestUtils {

    static final String MAIN_TOPIC = "filing-history-delta";
    static final String RETRY_TOPIC = "filing-history-delta-filing-history-delta-consumer-retry";
    static final String ERROR_TOPIC = "filing-history-delta-filing-history-delta-consumer-error";
    static final String INVALID_TOPIC = "filing-history-delta-filing-history-delta-consumer-invalid";

    private TestUtils() {
    }

    public static int noOfRecordsForTopic(ConsumerRecords<?, ?> records, String topic) {
        int count = 0;
        for (ConsumerRecord<?, ?> ignored : records.records(topic)) {
            count++;
        }
        return count;
    }
}
