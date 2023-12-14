package uk.gov.companieshouse.filinghistory.consumer.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.kafka.support.KafkaHeaders.EXCEPTION_MESSAGE;
import static org.springframework.kafka.support.KafkaHeaders.ORIGINAL_OFFSET;
import static org.springframework.kafka.support.KafkaHeaders.ORIGINAL_PARTITION;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.delta.ChsDelta;

@ExtendWith(MockitoExtension.class)
class InvalidMessageRouterTest {

    private InvalidMessageRouter invalidMessageRouter;

    @Mock
    private MessageFlags flags;

    @Mock
    private ChsDelta delta;

    @BeforeEach
    void setup() {
        invalidMessageRouter = new InvalidMessageRouter();
        invalidMessageRouter.configure(
                Map.of("message-flags", flags, "invalid-topic", "invalid"));
    }

    @Test
    void testOnSendRoutesMessageToProfileInvalidMessageTopicIfInvalidPayloadExceptionThrown() {
        // given
        ProducerRecord<String, ChsDelta> message = new ProducerRecord<>("main", 0, "key", delta,
                List.of(new RecordHeader(ORIGINAL_PARTITION, BigInteger.ZERO.toByteArray()),
                        new RecordHeader(ORIGINAL_OFFSET, BigInteger.ONE.toByteArray()),
                        new RecordHeader(EXCEPTION_MESSAGE, "invalid".getBytes())));

        ChsDelta invalidData = new ChsDelta("""
                { "invalid_message": "exception: [ invalid ] redirecting message from\s
                topic: main, partition: 0, offset: 1 to invalid topic" }
                """, 0, "", false);
        // when
        ProducerRecord<String, ChsDelta> actual = invalidMessageRouter.onSend(message);

        // then
        verify(flags, times(0)).destroy();
        assertThat(actual).isEqualTo(new ProducerRecord<>("invalid", "key", invalidData));
    }

    @Test
    void testOnSendRoutesMessageToTargetTopicIfRetryableExceptionThrown() {
        // given
        ProducerRecord<String, ChsDelta> message = new ProducerRecord<>("main", "key", delta);
        when(flags.isRetryable()).thenReturn(true);

        // when
        ProducerRecord<String, ChsDelta> actual = invalidMessageRouter.onSend(message);

        // then
        verify(flags, times(1)).destroy();
        assertThat(actual).isSameAs((message));
    }
}
