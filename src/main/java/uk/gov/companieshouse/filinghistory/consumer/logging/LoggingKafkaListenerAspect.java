package uk.gov.companieshouse.filinghistory.consumer.logging;

import static uk.gov.companieshouse.filinghistory.consumer.Application.NAMESPACE;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.filinghistory.consumer.exception.NonRetryableException;
import uk.gov.companieshouse.filinghistory.consumer.exception.RetryableException;
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
    private static final String RETRY_TOPIC_ATTEMPTS = "retry_topic-attempts";
    private static final String KAFKA_RECEIVED_TOPIC = "kafka_receivedTopic";
    private static final String KAFKA_RECEIVED_PARTITION_ID = "kafka_receivedPartitionId";
    private static final String KAFKA_OFFSET = "kafka_offset";
    private static final String RETRY_COUNT = "retry_count";

    private final int maxAttempts;

    LoggingKafkaListenerAspect(@Value("${consumer.max-attempts}") int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    @Around("@annotation(org.springframework.kafka.annotation.KafkaListener)")
    public Object manageStructuredLogging(ProceedingJoinPoint joinPoint) throws Throwable {

        try {
            Message<?> message = (Message<?>) joinPoint.getArgs()[0];
            ChsDelta chsDelta = extractChsDelta(message.getPayload());
            DataMapHolder.initialise(Optional.ofNullable(chsDelta.getContextId())
                    .orElse(UUID.randomUUID().toString()));

            DataMapHolder.get()
                    .retryCount(getAttempts(message.getHeaders().get(RETRY_TOPIC_ATTEMPTS)))
                    .topic((String) message.getHeaders().get(KAFKA_RECEIVED_TOPIC))
                    .partition((Integer) message.getHeaders().get(KAFKA_RECEIVED_PARTITION_ID))
                    .offset((Long) message.getHeaders().get(KAFKA_OFFSET));

            LOGGER.info(chsDelta.getIsDelete() ? LOG_MESSAGE_DELETE_RECEIVED : LOG_MESSAGE_RECEIVED,
                    DataMapHolder.getLogMap());

            Object result = joinPoint.proceed();

            LOGGER.info(chsDelta.getIsDelete() ? LOG_MESSAGE_DELETE_PROCESSED : LOG_MESSAGE_PROCESSED,
                    DataMapHolder.getLogMap());

            return result;
        } catch (RetryableException ex) {
            // maxAttempts includes first attempt which is not a retry
            if ((Integer) DataMapHolder.getLogMap().get(RETRY_COUNT) >= maxAttempts - 1) {
                LOGGER.error("Max retry attempts elapsed", ex, DataMapHolder.getLogMap());
            } else {
                LOGGER.info(EXCEPTION_MESSAGE.formatted(ex.getClass().getSimpleName()), DataMapHolder.getLogMap());
            }
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Exception thrown", ex, DataMapHolder.getLogMap());
            throw ex;
        } finally {
            DataMapHolder.clear();
        }
    }

    private int getAttempts(Object retryTopicAttempts) {
        int retryAttempts = 1;
        if (retryTopicAttempts != null) {
            byte[] value = (byte[]) retryTopicAttempts;
            if (value.length == 1) { // backwards compatibility
                retryAttempts = value[0];
            }
            if (value.length == 4) {
                retryAttempts = ByteBuffer.wrap(value).getInt();
            }
            LOGGER.info("Unexpected size for retry_topic-attempts header: %s".formatted(value.length), DataMapHolder.getLogMap());
        }
        return retryAttempts - 1;
    }

    private ChsDelta extractChsDelta(Object payload) {
        if (payload instanceof ChsDelta chsDelta) {
            return chsDelta;
        }
        throw new NonRetryableException("Invalid payload type. payload: %s".formatted(payload.toString()));
    }
}