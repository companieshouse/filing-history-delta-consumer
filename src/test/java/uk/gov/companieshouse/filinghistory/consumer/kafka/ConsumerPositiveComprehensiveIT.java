package uk.gov.companieshouse.filinghistory.consumer.kafka;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.requestMadeFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.KafkaUtils.ERROR_TOPIC;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.KafkaUtils.INVALID_TOPIC;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.KafkaUtils.MAIN_TOPIC;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.KafkaUtils.RETRY_TOPIC;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.delta.ChsDelta;

@SpringBootTest
@WireMockTest(httpPort = 8888)
class ConsumerPositiveComprehensiveIT extends AbstractKafkaIT {

    @Autowired
    private KafkaConsumer<String, byte[]> testConsumer;
    @Autowired
    private KafkaProducer<String, byte[]> testProducer;
    @Autowired
    private TestConsumerAspect testConsumerAspect;
    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("steps", () -> 1);
    }

    @BeforeEach
    public void setup() {
        testConsumerAspect.resetLatch();
        testConsumer.poll(Duration.ofSeconds(1));
    }

    @ParameterizedTest
    @CsvSource({
            "officers/TM01", "capital/SH07", "accounts/AA_rule_17", "insolvency/3.10", "insolvency/4.13",
            "insolvency/4.20_rule_2", "insolvency/4.31", "insolvency/4.33", "insolvency/4.35", "insolvency/4.38",
            "insolvency/4.40", "insolvency/4.43", "insolvency/WU15(Scot)", "insolvency/WU16(Scot)",
            "insolvency/WU17(Scot)", "insolvency/WU18(Scot)", "insolvency/4.44", "insolvency/4.46",
            "insolvency/4.48", "insolvency/4.51", "incorporation/OE01", "insolvency/4.68_rule_3",
            "insolvency/4.20_rule_1", "insolvency/4.68_rule_1", "insolvency/4.69", "insolvency/4.70",
            "insolvency/4.71", "insolvency/4.72", "insolvency/4.17(Scot)", "insolvency/4.9(Scot)",
            "insolvency/C04.2(Scot)", "insolvency/WU01", "insolvency/WU01(Scot)", "insolvency/C0LIQ",
            "insolvency/COCOMP_rule_2", "insolvency/WU07", "insolvency/WU08", "insolvency/WU09", "insolvency/WU11",
            "insolvency/WU12", "insolvency/WU14", "insolvency/AM11", "insolvency/2.24B_rule_1",
            "insolvency/2.24B_rule_2", "insolvency/2.26B", "insolvency/2.27B", "insolvency/2.28B", "insolvency/2.30B",
            "insolvency/AM20(Scot)", "insolvency/2.31B", "insolvency/2.32B", "insolvency/2.33B", "insolvency/2.34B",
            "insolvency/2.35B_rule_1", "insolvency/2.35B_rule_2", "insolvency/2.36B", "insolvency/2.38B",
            "insolvency/2.39B", "insolvency/AM16", "insolvency/AM17", "insolvency/AM18", "insolvency/2.40B",
            "insolvency/2.31B(Scot)"
    })
    void shouldConsumeFilingHistoryDeltaTopicAndProcessDelta(final String prefix) throws Exception {
        // given
        final String delta = IOUtils.resourceToString("/data/%s_delta.json".formatted(prefix), StandardCharsets.UTF_8);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().directBinaryEncoder(outputStream, null);
        DatumWriter<ChsDelta> writer = new ReflectDatumWriter<>(ChsDelta.class);
        writer.write(new ChsDelta(delta, 0, "context_id", false), encoder);

        final String expectedRequestBody = IOUtils.resourceToString("/data/%s_request_body.json".formatted(prefix),
                StandardCharsets.UTF_8);
        InternalFilingHistoryApi request = objectMapper.readValue(expectedRequestBody, InternalFilingHistoryApi.class);

        final String expectedRequestUri = "/filing-history-data-api/company/%s/filing-history/%s/internal".formatted(
                request.getInternalData().getCompanyNumber(),
                request.getExternalData().getTransactionId());

        stubFor(put(urlEqualTo(expectedRequestUri))
                .willReturn(aResponse()
                        .withStatus(200)));

        // when
        testProducer.send(new ProducerRecord<>(MAIN_TOPIC, 0, System.currentTimeMillis(),
                "key", outputStream.toByteArray()));
        if (!testConsumerAspect.getLatch().await(5L, TimeUnit.SECONDS)) {
            fail("Timed out waiting for latch");
        }

        // then
        ConsumerRecords<?, ?> consumerRecords = KafkaTestUtils.getRecords(testConsumer, Duration.ofMillis(10000L), 1);
        assertThat(KafkaUtils.noOfRecordsForTopic(consumerRecords, MAIN_TOPIC)).isOne();
        assertThat(KafkaUtils.noOfRecordsForTopic(consumerRecords, RETRY_TOPIC)).isZero();
        assertThat(KafkaUtils.noOfRecordsForTopic(consumerRecords, ERROR_TOPIC)).isZero();
        assertThat(KafkaUtils.noOfRecordsForTopic(consumerRecords, INVALID_TOPIC)).isZero();

        verify(requestMadeFor(new PutRequestMatcher(expectedRequestUri, expectedRequestBody)));
    }
}
