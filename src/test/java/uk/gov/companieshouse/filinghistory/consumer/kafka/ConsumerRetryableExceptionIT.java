package uk.gov.companieshouse.filinghistory.consumer.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.TestUtils.ERROR_TOPIC;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.TestUtils.INVALID_TOPIC;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.TestUtils.MAIN_TOPIC;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.TestUtils.RETRY_TOPIC;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.filinghistory.consumer.delta.Service;
import uk.gov.companieshouse.filinghistory.consumer.exception.RetryableException;

@SpringBootTest
@ActiveProfiles("test_main_retryable")
class ConsumerRetryableExceptionIT extends AbstractKafkaIT {

    @Autowired
    private KafkaConsumer<String, byte[]> testConsumer;

    @Autowired
    private KafkaProducer<String, byte[]> testProducer;

    @Autowired
    private CountDownLatch latch;

    @MockBean
    private Service service;

    @BeforeEach
    public void setup() {
        testConsumer.poll(Duration.ofSeconds(1));
    }

    @Test
    void testRepublishToCompanyProfileErrorTopicThroughRetryTopics() throws Exception {
        //given
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().directBinaryEncoder(outputStream, null);
        DatumWriter<ChsDelta> writer = new ReflectDatumWriter<>(ChsDelta.class);
        writer.write(new ChsDelta("", 0, "context_id", false), encoder);

        doThrow(RetryableException.class).when(service).process(any());

        //when
        testProducer.send(new ProducerRecord<>(MAIN_TOPIC, 0, System.currentTimeMillis(),
                "key", outputStream.toByteArray()));
        if (!latch.await(5L, TimeUnit.SECONDS)) {
            fail("Timed out waiting for latch");
        }

        //then
        ConsumerRecords<?, ?> consumerRecords = KafkaTestUtils.getRecords(testConsumer, Duration.ofMillis(10000L), 6);
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, MAIN_TOPIC)).isEqualTo(1);
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, RETRY_TOPIC))
                .isEqualTo(4);
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, ERROR_TOPIC))
                .isEqualTo(1);
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, INVALID_TOPIC)).isZero();
        verify(service, times(5)).process(any());
    }
}
