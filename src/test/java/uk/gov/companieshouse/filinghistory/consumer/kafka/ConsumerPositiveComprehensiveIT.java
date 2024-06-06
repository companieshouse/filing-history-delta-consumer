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
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
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
        testConsumer.poll(KafkaUtils.kafkaPollingDuration());
    }

    // FIXME: RP04SLPPSC04, RP04SLPPSC05, RP04SQPPSC04
    //  failing due to missing psc_name in description value object on delta
    @ParameterizedTest
    @CsvSource({
//            "change-of-name/CONDIR",
//
//            "resolution/RES01", "resolution/RES01_no_barcode", "resolution/RES15_top_level", "resolution/RES15_child",
//
//            "annotation/annotation", "annotation/top_level_annotation",
//
//            "annual_return/363s",
//
//            "officers/EW01RSS", "officers/TM01", "officers/RP04LLPSC05", "officers/RP04LLPSC04", "officers/RP04PSC09",
//            "officers/RP04PSC07", "officers/RP04PSC03", "officers/RP04LLPSC02", "officers/RP04LLPSC03",
//            "officers/RP04PSC01", "officers/RP04PSC04", "officers/RP04PSC02", "officers/RP04LLPSC01",
//            "officers/RP04PSC05", "officers/RP04LLPSC07",
//
//            "capital/SH03", "capital/SH07", "capital/SH01", "capital/SH02_rule_2", "capital/SH04_rule_4",
//            "capital/SH05", "capital/EW05RSS",
//
//            "accounts/AA_rule_17", "accounts/AA_rule_9", "accounts/AA_rule_10", "accounts/AA_rule_8",
//            "accounts/AA_rule_6", "accounts/AA_rule_12", "accounts/AA_rule_14", "accounts/AA_rule_20",
//            "accounts/AA_rule_23", "accounts/AA_rule_25", "accounts/AA_rule_26", "accounts/AAMD_rule_12",
//            "accounts/AAMD_rule_9", "accounts/AAMD_rule_1", "accounts/AAMD_rule_7",
//
//            "address/287",
//
//            "incorporation/child_transaction/model_articles", "incorporation/child_transaction/newinc",
//
//            "insolvency/3.10", "insolvency/4.13", "insolvency/4.20_rule_2", "insolvency/4.31", "insolvency/4.33",
//            "insolvency/4.35", "insolvency/4.38", "insolvency/4.40", "insolvency/4.43", "insolvency/WU15(Scot)",
//            "insolvency/WU16(Scot)", "insolvency/WU17(Scot)", "insolvency/WU18(Scot)", "insolvency/4.44",
//            "insolvency/4.46", "insolvency/4.48", "insolvency/4.51", "incorporation/OE01", "insolvency/4.68_rule_3",
//            "insolvency/4.20_rule_1", "insolvency/4.68_rule_1", "insolvency/4.69", "insolvency/4.70", "insolvency/4.71",
//            "insolvency/4.72", "insolvency/4.17(Scot)", "insolvency/4.9(Scot)", "insolvency/C04.2(Scot)",
//            "insolvency/WU01", "insolvency/WU01(Scot)", "insolvency/C0LIQ", "insolvency/COCOMP_rule_2",
//            "insolvency/WU07", "insolvency/WU08", "insolvency/WU09", "insolvency/WU11", "insolvency/WU12",
//            "insolvency/WU14", "insolvency/AM11", "insolvency/2.24B_rule_1", "insolvency/2.24B_rule_2",
//            "insolvency/2.26B", "insolvency/2.27B", "insolvency/2.28B", "insolvency/2.30B", "insolvency/AM20(Scot)",
//            "insolvency/2.31B", "insolvency/2.32B", "insolvency/2.33B", "insolvency/2.34B", "insolvency/2.35B_rule_1",
//            "insolvency/2.35B_rule_2", "insolvency/2.36B", "insolvency/2.38B", "insolvency/2.39B", "insolvency/AM16",
//            "insolvency/AM17", "insolvency/AM18", "insolvency/2.40B", "insolvency/2.31B(Scot)", "insolvency/2.12B",
//            "insolvency/AM11(Scot)", "insolvency/AM12", "insolvency/2.16B_rule_1", "insolvency/AM02(Scot)_rule_1",
//            "insolvency/2.16B_rule_2", "insolvency/2.17B", "insolvency/AM04(Scot)", "insolvency/AM05(Scot)",
//            "insolvency/2.22B", "insolvency/2.23B", "insolvency/AM08(Scot)", "insolvency/AM07(Scot)",
//            "insolvency/AM01(Scot)", "insolvency/2.15B_rule_1", "insolvency/2.15B_rule_2", "insolvency/2.16B(Scot)",
//            "insolvency/AM03(Scot)", "insolvency/2.17B(Scot)", "insolvency/AM09(Scot)", "insolvency/2.16BZ(Scot)",
//            "insolvency/2.20B(Scot)_rule_2", "insolvency/2.19B(Scot)", "insolvency/2.21B(Scot)",
//            "insolvency/2.22B(Scot)", "insolvency/2.23B(Scot)", "insolvency/AM21(Scot)", "insolvency/2.24B(Scot)",
//            "insolvency/AM25(Scot)", "insolvency/2.25B(Scot)", "insolvency/AM22(Scot)",
//            "insolvency/2.26B(Scot)",
//            "insolvency/1(Scot)", "insolvency/1.3(Scot)_rule_1", "insolvency/1.3(Scot)_rule_2",
//            "insolvency/1.4(Scot)", "insolvency/2.2(Scot)", "insolvency/2.12(Scot)", "insolvency/2.26B(Scot)",
//            "insolvency/2.27B(Scot)", "insolvency/2.29B(Scot)", "insolvency/2.30B(Scot)", "insolvency/12.1",
//            "insolvency/AM15(Scot)", "insolvency/AM23(Scot)", "insolvency/NCOP", "insolvency/RM01(Scot)",
//            "mortgage/MR01_rule_4", "mortgage/MR01_rule_5", "mortgage/MR02_rule_3", "mortgage/MR02_rule_4",
//            "mortgage/MR03_rule_4",
//            "mortgage/MR04_rule_1", "mortgage/MR04_rule_2", "mortgage/MR04_rule_3", "mortgage/MR05_rule_1",
//            "mortgage/MR05_rule_2", "mortgage/MR05_rule_3", "mortgage/MR05_rule_4", "mortgage/MR05_rule_5",
//            "mortgage/MR05_rule_6", "mortgage/MR05_rule_7", "mortgage/MR06", "mortgage/MR07", "mortgage/MR08",
//            "mortgage/MR09", "mortgage/MR10", "mortgage/LLMR03", "mortgage/466(Scot)", "mortgage/LLP466(Scot)",
//            "mortgage/402R(NI)",

            "mortgage/LLPSC07", "mortgage/LLPSC07_no_description_values"

//            "mortgage/PSC07"

            // New failing tests below here
//            "officers/RP04SLPPSC04"
//            "officers/RP04SLPPSC05"
//            "officers/RP04SQPPSC04"
    })
    void shouldConsumeFilingHistoryDeltaTopicAndProcessDeltaFromCSV(final String prefix) throws Exception {
        final String delta = IOUtils.resourceToString("/data/%s_delta.json".formatted(prefix), StandardCharsets.UTF_8);
        final String expectedRequestBody = IOUtils.resourceToString("/data/%s_request_body.json".formatted(prefix),
                StandardCharsets.UTF_8);
        doShouldConsumeFilingHistoryDeltaTopicAndProcessDelta(delta, expectedRequestBody);
    }

    @ParameterizedTest(name = "[{index}] {0}/{1}/{2}")
    @ArgumentsSource(IntegrationDataGenerator.class)
    @EnabledIfEnvironmentVariable(disabledReason = "Disabled for normal builds", named = "RUN_BULK_TEST",
            matches = "^(1|true|TRUE)$")
    void shouldConsumeFilingHistoryDeltaTopicAndProcessDeltaFromStream(String category, String formType,
            String entityId, String delta, String expectedRequestBody) throws Exception {
        doShouldConsumeFilingHistoryDeltaTopicAndProcessDelta(delta, expectedRequestBody);
    }

    private void doShouldConsumeFilingHistoryDeltaTopicAndProcessDelta(final String delta,
            final String expectedRequestBody) throws Exception {
        // given
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().directBinaryEncoder(outputStream, null);
        DatumWriter<ChsDelta> writer = new ReflectDatumWriter<>(ChsDelta.class);
        writer.write(new ChsDelta(delta, 0, "context_id", false), encoder);

        InternalFilingHistoryApi request = objectMapper.readValue(expectedRequestBody, InternalFilingHistoryApi.class);

        final String expectedRequestUri = "/company/%s/filing-history/%s/internal".formatted(
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
