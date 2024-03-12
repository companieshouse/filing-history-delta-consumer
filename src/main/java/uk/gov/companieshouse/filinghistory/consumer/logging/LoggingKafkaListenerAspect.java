package uk.gov.companieshouse.filinghistory.consumer.logging;

import static uk.gov.companieshouse.filinghistory.consumer.Application.NAMESPACE;

import java.util.Optional;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.filinghistory.consumer.exception.NonRetryableException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
@Aspect
class LoggingKafkaListenerAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private static final String LOG_MESSAGE_RECEIVED = "Processing delta";
    private static final String LOG_MESSAGE_DELETE_RECEIVED = "Processing DELETE delta";
    private static final String LOG_MESSAGE_PROCESSED = "Processed delta";
    private static final String LOG_MESSAGE_DELETE_PROCESSED = "Processed DELETE delta";
    private static final String EXCEPTION_MESSAGE = "%s exception thrown";

    @Around("@annotation(org.springframework.kafka.annotation.KafkaListener)")
    public Object manageStructuredLogging(ProceedingJoinPoint joinPoint) throws Throwable {

        try {
            Message<?> message = (Message<?>) joinPoint.getArgs()[0];
            ChsDelta chsDelta = extractChsDelta(message.getPayload());
            DataMapHolder.initialise(Optional.ofNullable(chsDelta.getContextId())
                    .orElse(UUID.randomUUID().toString()));

            DataMapHolder.get()
                    .topic((String) message.getHeaders().get("kafka_receivedTopic"))
                    .partition((Integer) message.getHeaders().get("kafka_receivedPartitionId"))
                    .offset((Long) message.getHeaders().get("kafka_offset"));

            LOGGER.info(chsDelta.getIsDelete() ? LOG_MESSAGE_DELETE_RECEIVED : LOG_MESSAGE_RECEIVED,
                    DataMapHolder.getLogMap());

            Object result = joinPoint.proceed();

            LOGGER.info(chsDelta.getIsDelete() ? LOG_MESSAGE_DELETE_PROCESSED : LOG_MESSAGE_PROCESSED,
                    DataMapHolder.getLogMap());

            return result;
        } catch (Exception ex) {
            LOGGER.error(EXCEPTION_MESSAGE.formatted(ex.getClass().getSimpleName()), ex, DataMapHolder.getLogMap());
            throw ex;
        } finally {
            DataMapHolder.clear();
        }
    }

    private ChsDelta extractChsDelta(Object payload) {
        if (payload instanceof ChsDelta chsDelta) {
            return chsDelta;
        }
        throw new NonRetryableException("Invalid payload type. payload: %s".formatted(payload.toString()));
    }
}