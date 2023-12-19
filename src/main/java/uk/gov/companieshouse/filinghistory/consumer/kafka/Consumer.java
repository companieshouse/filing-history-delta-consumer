package uk.gov.companieshouse.filinghistory.consumer.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.filinghistory.consumer.delta.Service;
import uk.gov.companieshouse.filinghistory.consumer.exception.RetryableException;

@Component
public class Consumer {

    private final Service service;
    private final MessageFlags messageFlags;

    public Consumer(Service service, MessageFlags messageFlags) {
        this.service = service;
        this.messageFlags = messageFlags;
    }

    @KafkaListener(
            id = "${consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory",
            topics = {"${consumer.topic}"},
            groupId = "${consumer.group-id}"
    )
    public void consume(Message<ChsDelta> message) {
        try {
            service.process(message.getPayload());
        } catch (RetryableException exception) {
            messageFlags.setRetryable(true);
            throw exception;
        }
    }
}
