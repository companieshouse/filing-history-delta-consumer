package uk.gov.companieshouse.filinghistory.consumer.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.KafkaUtils.ERROR_TOPIC;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.KafkaUtils.INVALID_TOPIC;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.KafkaUtils.MAIN_TOPIC;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.KafkaUtils.RETRY_TOPIC;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
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
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.api.handler.delta.PrivateDeltaResourceHandler;
import uk.gov.companieshouse.api.handler.delta.filinghistory.request.PrivateFilingHistoryPut;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.filinghistory.consumer.delta.ResponseHandler;

@SpringBootTest
class ConsumerPositiveIT extends AbstractKafkaIT {

    private static final String TRANSACTION_ID = "MzA0Mzk3MjY3NXNhbHQ";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String PUT_REQUEST_URI = "/filing-history-data-api/company/%s/filing-history/%s/internal".formatted(COMPANY_NUMBER, TRANSACTION_ID);

    @Autowired
    private KafkaConsumer<String, byte[]> testConsumer;
    @Autowired
    private KafkaProducer<String, byte[]> testProducer;
    @Autowired
    private LatchAspect latchAspect;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private Supplier<InternalApiClient> internalApiClientSupplier;
    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private HttpClient apiClient;
    @Mock
    private PrivateDeltaResourceHandler privateDeltaResourceHandler;
    @Mock
    private PrivateFilingHistoryPut privateFilingHistoryPut;
    @Mock
    private ResponseHandler responseHandler;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("steps", () -> 1);
    }

    @BeforeEach
    public void setup() {
        testConsumer.poll(Duration.ofSeconds(1));
    }

    @ParameterizedTest
    @CsvSource({
            "TM01"
    })
    void testConsumeFromStreamFilingHistoryDeltaTopic(final String prefix) throws Exception {
        //given
        final String delta = IOUtils.resourceToString("/%s_delta.json".formatted(prefix), StandardCharsets.UTF_8);
        final String requestBody = IOUtils.resourceToString("/%s_request_body.json".formatted(prefix), StandardCharsets.UTF_8);
        final InternalFilingHistoryApi expectedRequestBody = objectMapper.readValue(requestBody, InternalFilingHistoryApi.class);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().directBinaryEncoder(outputStream, null);
        DatumWriter<ChsDelta> writer = new ReflectDatumWriter<>(ChsDelta.class);
        writer.write(new ChsDelta(delta, 0, "context_id", false), encoder);

        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(apiClient);
        when(internalApiClient.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.putFilingHistory(any(), any())).thenReturn(privateFilingHistoryPut);

        //when
        testProducer.send(new ProducerRecord<>(MAIN_TOPIC, 0, System.currentTimeMillis(),
                "key", outputStream.toByteArray()));
        if (!latchAspect.getLatch().await(5L, TimeUnit.SECONDS)) {
            fail("Timed out waiting for latch");
        }

        //then
        ConsumerRecords<?, ?> consumerRecords = KafkaTestUtils.getRecords(testConsumer, Duration.ofMillis(10000L), 1);
        assertThat(KafkaUtils.noOfRecordsForTopic(consumerRecords, MAIN_TOPIC)).isOne();
        assertThat(KafkaUtils.noOfRecordsForTopic(consumerRecords, RETRY_TOPIC)).isZero();
        assertThat(KafkaUtils.noOfRecordsForTopic(consumerRecords, ERROR_TOPIC)).isZero();
        assertThat(KafkaUtils.noOfRecordsForTopic(consumerRecords, INVALID_TOPIC)).isZero();

        verify(privateDeltaResourceHandler).putFilingHistory(PUT_REQUEST_URI, expectedRequestBody);
        verify(privateFilingHistoryPut).execute();
        verifyNoInteractions(responseHandler);
    }
}
