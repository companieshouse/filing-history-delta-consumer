package uk.gov.companieshouse.filinghistory.api.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static uk.gov.companieshouse.filinghistory.api.kafka.TopicUtils.ERROR_TOPIC;
import static uk.gov.companieshouse.filinghistory.api.kafka.TopicUtils.INVALID_TOPIC;
import static uk.gov.companieshouse.filinghistory.api.kafka.TopicUtils.MAIN_TOPIC;
import static uk.gov.companieshouse.filinghistory.api.kafka.TopicUtils.RETRY_TOPIC;
import static uk.gov.companieshouse.filinghistory.api.kafka.TopicUtils.countTopicRecords;

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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.filinghistory.consumer.Application;
import uk.gov.companieshouse.filinghistory.consumer.delta.Service;

@SpringBootTest(classes = Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(
        topics = {MAIN_TOPIC,
                RETRY_TOPIC,
                ERROR_TOPIC,
                INVALID_TOPIC},
        controlledShutdown = true,
        partitions = 1
)
class ConsumerPositiveITest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaConsumer<String, ChsDelta> consumer;

    @Autowired
    private KafkaProducer<String, Object> producer;

    @Autowired
    private CountDownLatch latch;

    @MockBean
    private Service service;

    @Test
    void testConsumeFromMainTopic() throws Exception {
        //given
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().directBinaryEncoder(outputStream, null);
        DatumWriter<ChsDelta> writer = new ReflectDatumWriter<>(ChsDelta.class);
        writer.write(new ChsDelta("{}", 0, "context_id", false), encoder);

        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);

        //when
        producer.send(
                new ProducerRecord<>(MAIN_TOPIC, 0, System.currentTimeMillis(),
                        "key", outputStream.toByteArray()));
        if (!latch.await(5L, TimeUnit.SECONDS)) {
            fail("Timed out waiting for latch");
        }

        //then
        ConsumerRecords<?, ?> consumerRecords = KafkaTestUtils.getRecords(consumer, Duration.ofMillis(10000L), 1);
        assertThat(countTopicRecords(consumerRecords, MAIN_TOPIC)).isEqualTo(1);
        assertThat(countTopicRecords(consumerRecords, RETRY_TOPIC)).isZero();
        assertThat(countTopicRecords(consumerRecords, ERROR_TOPIC)).isZero();
        assertThat(countTopicRecords(consumerRecords, INVALID_TOPIC)).isZero();
        verify(service).process(any());
    }
}
