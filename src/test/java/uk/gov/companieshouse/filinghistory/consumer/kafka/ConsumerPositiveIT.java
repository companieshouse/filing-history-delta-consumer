package uk.gov.companieshouse.filinghistory.consumer.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static uk.gov.companieshouse.api.filinghistory.ExternalData.CategoryEnum.OFFICERS;
import static uk.gov.companieshouse.api.filinghistory.ExternalData.SubcategoryEnum.TERMINATION;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.KafkaUtils.ERROR_TOPIC;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.KafkaUtils.INVALID_TOPIC;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.KafkaUtils.MAIN_TOPIC;
import static uk.gov.companieshouse.filinghistory.consumer.kafka.KafkaUtils.RETRY_TOPIC;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataLinks;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalDataOriginalValues;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.filinghistory.consumer.delta.DeltaServiceRouter;

@SpringBootTest
class ConsumerPositiveIT extends AbstractKafkaIT {

    private static final String TRANSACTION_ID = "MzA0Mzk3MjY3NXNhbHQ";
    private static final String COMPANY_NUMBER = "12345678";

    @Autowired
    private KafkaConsumer<String, byte[]> testConsumer;

    @Autowired
    private KafkaProducer<String, byte[]> testProducer;

    @Autowired
    private LatchAspect latchAspect;

    @MockBean
    private DeltaServiceRouter router;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("steps", () -> 1);
    }

    @BeforeEach
    public void setup() {
        testConsumer.poll(Duration.ofSeconds(1));
    }

    @Test
    void testConsumeFromStreamFilingHistoryDeltaTopic() throws Exception {
        //given
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().directBinaryEncoder(outputStream, null);
        DatumWriter<ChsDelta> writer = new ReflectDatumWriter<>(ChsDelta.class);
        writer.write(new ChsDelta("", 0, "context_id", false), encoder);

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

        // TODO: Remove Mockbean and verify api request body
        verify(router).route(any());
    }

    private static InternalFilingHistoryApi buildExpectedRequestBody() {
        // TODO: Replace incorrect values once transform rules are fully implemented e.g. [% officerName | title_case  %] or the TODOs
        return new InternalFilingHistoryApi()
                .externalData(new ExternalData()
                        .transactionId(TRANSACTION_ID)
                        .barcode("XHJYVXAY")
                        .type("TM01")
                        .date("20120604053919")
                        .category(OFFICERS)
                        .subcategory(TERMINATION)
                        .description("termination-director-company-with-name-termination-date")
                        .descriptionValues(new FilingHistoryItemDataDescriptionValues()
                                .officerName("[% officerName | title_case  %]")
                                .terminationDate("TODO: BSON date: terminationDate"))
                        .paperFiled(false)
                        .actionDate("TODO: BSON date: terminationDate")
                        .links(new FilingHistoryItemDataLinks()
                                .self("/company/%s/filing-history/%s".formatted(COMPANY_NUMBER, TRANSACTION_ID))))
                .internalData(new InternalData()
                        .originalValues(new InternalDataOriginalValues()
                                .resignationDate("04/06/2012")
                                .officerName("John Tester"))
                        .originalDescription("TODO: Sentence case: Appointment Terminated, Director JOHN TESTER")
                        .companyNumber(COMPANY_NUMBER)
                        .parentEntityId("")
                        .entityId("3043972675")
                        .documentId("000XHJYVXAY4378")
                        .deltaAt("20120705053919")
                        .updatedBy("context_id")
                        .transactionKind(InternalData.TransactionKindEnum.TOP_LEVEL));
    }

}
