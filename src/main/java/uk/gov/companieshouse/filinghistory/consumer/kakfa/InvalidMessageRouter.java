package uk.gov.companieshouse.filinghistory.consumer.kakfa;

import static uk.gov.companieshouse.filinghistory.consumer.Application.NAMESPACE;

import java.util.Map;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

public class InvalidMessageRouter implements ProducerInterceptor<String, Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private MessageFlags messageFlags;
    private String invalidTopic;

    @Override
    public ProducerRecord<String, Object> onSend(ProducerRecord<String, Object> producerRecord) {
        if (messageFlags.isRetryable()) {
            messageFlags.destroy();
            return producerRecord;
        } else {
            LOGGER.info("Moving record to invalid topic: [%s]%nMessage content: %s".formatted(invalidTopic,
                    producerRecord.value()));
            return new ProducerRecord<>(invalidTopic, producerRecord.key(), producerRecord.topic());
        }
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
        //
    }

    @Override
    public void close() {
        //
    }

    @Override
    public void configure(Map<String, ?> configs) {
        this.messageFlags = (MessageFlags) configs.get("message.flags");
        this.invalidTopic = (String) configs.get("invalid.topic");
    }
}
