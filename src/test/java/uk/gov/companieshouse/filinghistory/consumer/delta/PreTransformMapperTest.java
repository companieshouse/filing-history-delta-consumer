package uk.gov.companieshouse.filinghistory.consumer.delta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.DescriptionValues;
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.api.delta.FilingHistoryDelta;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.FormatDate;

@ExtendWith(MockitoExtension.class)
class PreTransformMapperTest {

    private PreTransformMapper preTransformMapper;
    @Mock
    private FormatDate formatDate;

    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .registerModule(new JavaTimeModule());


    @BeforeEach
    void setUp() {
        preTransformMapper = new PreTransformMapper(objectMapper, formatDate);
    }

    @Test
    void shouldMapDeltaObjectOntoObjectNode() {
        // given
        final FilingHistoryDelta delta = new FilingHistoryDelta()
                .deltaAt("20140916230459600643")
                .filingHistory(List.of(
                        new FilingHistory()
                                .category("2")
                                .receiveDate("20110905053919")
                                .formType("TM01")
                                .description("Appointment Terminated, Director JOHN DOE")
                                .barcode("XAITVXAX")
                                .documentId("000XAITVXAX4682")
                                .companyNumber("12345678")
                                .entityId("3063732185")
                                .parentEntityId("")
                                .parentFormType("")
                                .descriptionValues(new DescriptionValues()
                                        .resignationDate("02/07/2011")
                                        .OFFICER_NAME("John Doe"))
                                .preScannedBatch("0")
                ));

        final ObjectNode expectedTopLevelNode = objectMapper.createObjectNode()
                .put("company_number", "12345678")
                .put("_entity_id", "3063732185")
                .put("_barcode", "XAITVXAX")
                .put("_document_id", "000XAITVXAX4682")
                .put("parent_entity_id", "")
                .put("parent_form_type", "")
                .put("pre_scanned_batch", "0");

        expectedTopLevelNode
                .putObject("original_values")
                .put("resignation_date", "02/07/2011")
                .put("officer_name", "John Doe");

        expectedTopLevelNode
                .putObject("data")
                .put("type", "TM01")
                .put("date", "2011-09-05T05:39:19Z")
                .put("description", "Appointment Terminated, Director JOHN DOE")
                .put("category", "2");

        when(formatDate.format(any())).thenReturn("2011-09-05T05:39:19Z");

        // when
        final ObjectNode actualObjectNode = preTransformMapper.mapDeltaToObjectNode(
                delta.getFilingHistory().getFirst());

        // then
        assertEquals(expectedTopLevelNode, actualObjectNode);
        verify(formatDate).format("20110905053919");
    }

    @Test
    void shouldMapDeltaObjectOntoObjectNodeWhenNullDescriptionValues() {
        // given
        final FilingHistoryDelta delta = new FilingHistoryDelta()
                .deltaAt("20140916230459600643")
                .filingHistory(List.of(
                        new FilingHistory()
                                .category("2")
                                .receiveDate("20110905053919")
                                .formType("TM01")
                                .description("Appointment Terminated, Director JOHN DOE")
                                .barcode("")
                                .documentId("")
                                .descriptionValues(null)
                                .companyNumber("12345678")
                                .entityId("3063732185")
                                .parentEntityId("")
                                .parentFormType("")
                                .preScannedBatch("0")
                ));

        final ObjectNode expectedTopLevelNode = objectMapper.createObjectNode()
                .put("company_number", "12345678")
                .put("_entity_id", "3063732185")
                .put("pre_scanned_batch", "0")
                .put("parent_entity_id", "")
                .put("parent_form_type", "");

        expectedTopLevelNode
                .putObject("data")
                .put("type", "TM01")
                .put("date", "2011-09-05T05:39:19Z")
                .put("description", "Appointment Terminated, Director JOHN DOE")
                .put("category", "2");

        when(formatDate.format(any())).thenReturn("2011-09-05T05:39:19Z");

        // when
        final ObjectNode actualObjectNode = preTransformMapper.mapDeltaToObjectNode(
                delta.getFilingHistory().getFirst());

        // then
        assertEquals(expectedTopLevelNode, actualObjectNode);
        verify(formatDate).format("20110905053919");
    }

    @ParameterizedTest
    @CsvSource({
            "02/07/2011 , ",
            " , John Doe"
    })
    void shouldMapDeltaObjectOntoObjectNodeWhenDeltaMissingFieldsOnDescriptionValues(final String resignationDate,
            final String officerName) {
        // given
        final FilingHistoryDelta delta = new FilingHistoryDelta()
                .deltaAt("20140916230459600643")
                .filingHistory(List.of(
                        new FilingHistory()
                                .category("2")
                                .receiveDate("20110905053919")
                                .formType("TM01")
                                .description("Appointment Terminated, Director JOHN DOE")
                                .barcode("")
                                .documentId("")
                                .descriptionValues(new DescriptionValues()
                                        .resignationDate(resignationDate)
                                        .OFFICER_NAME(officerName))
                                .companyNumber("12345678")
                                .entityId("3063732185")
                                .parentEntityId("")
                                .parentFormType("")
                                .preScannedBatch("0")
                ));

        final ObjectNode expectedTopLevelNode = objectMapper.createObjectNode()
                .put("company_number", "12345678")
                .put("_entity_id", "3063732185")
                .put("pre_scanned_batch", "0")
                .put("parent_entity_id", "")
                .put("parent_form_type", "");

        expectedTopLevelNode
                .putObject("data")
                .put("type", "TM01")
                .put("date", "2011-09-05T05:39:19Z")
                .put("description", "Appointment Terminated, Director JOHN DOE")
                .put("category", "2");

        when(formatDate.format(any())).thenReturn("2011-09-05T05:39:19Z");

        // when
        final ObjectNode actualObjectNode = preTransformMapper.mapDeltaToObjectNode(
                delta.getFilingHistory().getFirst());

        // then
        assertEquals(expectedTopLevelNode, actualObjectNode);
        verify(formatDate).format("20110905053919");
    }

    @Test
    void shouldMapDeltaObjectOntoObjectNodeWhenDeltaMissingFieldsAndEmptyDescriptionValuesObject() {
        // given
        final FilingHistoryDelta delta = new FilingHistoryDelta()
                .deltaAt("20140916230459600643")
                .filingHistory(List.of(
                        new FilingHistory()
                                .category("2")
                                .receiveDate("20110905053919")
                                .formType("TM01")
                                .description("Appointment Terminated, Director JOHN DOE")
                                .barcode("")
                                .documentId("")
                                .descriptionValues(new DescriptionValues())
                                .companyNumber("12345678")
                                .entityId("3063732185")
                                .parentEntityId("")
                                .parentFormType("")
                                .preScannedBatch("0")
                ));

        final ObjectNode expectedTopLevelNode = objectMapper.createObjectNode()
                .put("company_number", "12345678")
                .put("_entity_id", "3063732185")
                .put("parent_entity_id", "")
                .put("parent_form_type", "")
                .put("pre_scanned_batch", "0");

        expectedTopLevelNode
                .putObject("data")
                .put("type", "TM01")
                .put("date", "2011-09-05T05:39:19Z")
                .put("description", "Appointment Terminated, Director JOHN DOE")
                .put("category", "2");

        when(formatDate.format(any())).thenReturn("2011-09-05T05:39:19Z");

        // when
        final ObjectNode actualObjectNode = preTransformMapper.mapDeltaToObjectNode(
                delta.getFilingHistory().getFirst());

        // then
        assertEquals(expectedTopLevelNode, actualObjectNode);
        verify(formatDate).format("20110905053919");
    }
}
