package uk.gov.companieshouse.filinghistory.consumer.kafka;

import static org.springframework.kafka.support.KafkaHeaders.EXCEPTION_MESSAGE;
import static org.springframework.kafka.support.KafkaHeaders.ORIGINAL_OFFSET;
import static org.springframework.kafka.support.KafkaHeaders.ORIGINAL_PARTITION;
import static uk.gov.companieshouse.filinghistory.consumer.Application.NAMESPACE;

import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

public class InvalidMessageRouter implements ProducerInterceptor<String, ChsDelta> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private MessageFlags messageFlags;
    private String invalidTopic;

    @Override
    public ProducerRecord<String, ChsDelta> onSend(ProducerRecord<String, ChsDelta> producerRecord) {
        if (messageFlags.isRetryable()) {
            messageFlags.destroy();
            return producerRecord;
        } else {

            String originalTopic = producerRecord.topic();
            BigInteger partition = Optional.ofNullable(producerRecord.headers().lastHeader(ORIGINAL_PARTITION))
                    .map(h -> new BigInteger(h.value())).orElse(BigInteger.valueOf(-1));
            BigInteger offset = Optional.ofNullable(producerRecord.headers().lastHeader(ORIGINAL_OFFSET))
                    .map(h -> new BigInteger(h.value())).orElse(BigInteger.valueOf(-1));
            String exception = Optional.ofNullable(producerRecord.headers().lastHeader(EXCEPTION_MESSAGE))
                    .map(h -> new String(h.value())).orElse("unknown");

            ChsDelta invalidData = new ChsDelta("""
                    { "invalid_message": "exception: [ %s ] redirecting message from\s
                    topic: %s, partition: %d, offset: %d to invalid topic" }
                    """.formatted(exception, originalTopic, partition, offset), 0, "", false);

            LOGGER.info(String.format("Moving record into topic: [%s]%nMessage content: %s", invalidTopic,
                    invalidData.getData()));

            return new ProducerRecord<>(invalidTopic, producerRecord.key(), invalidData);
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
        this.messageFlags = (MessageFlags) configs.get("message-flags");
        this.invalidTopic = (String) configs.get("invalid-topic");
    }
}
