package uk.gov.companieshouse.filinghistory.consumer.delta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.DescriptionValues;
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.api.delta.FilingHistoryDelta;

@ExtendWith(MockitoExtension.class)
class PreTransformMapperTest {

    @InjectMocks
    private PreTransformMapper preTransformMapper;

    @Mock
    private ObjectMapper objectMapper;

    private final ObjectMapper testObjectMapper =
            new ObjectMapper()
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .registerModule(new JavaTimeModule());

    @Test
    void shouldMapDeltaObjectOntoObjectNode() {
        // given
        when(objectMapper.createObjectNode()).thenReturn(testObjectMapper.createObjectNode());

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

        final ObjectNode expectedObjectNode = testObjectMapper.createObjectNode();
        expectedObjectNode.put("company_number", "12345678");
        expectedObjectNode.put("_entity_id", "3063732185");
        expectedObjectNode.put("_barcode", "XAITVXAX");
        expectedObjectNode.put("_document_id", "000XAITVXAX4682");

        ObjectNode expectedOriginalValuesNode = expectedObjectNode.putObject("original_values");

        ObjectNode expectedDataNode = expectedObjectNode.putObject("data");

        expectedOriginalValuesNode.put("resignation_date", "02/07/2011");
        expectedOriginalValuesNode.put("officer_name", "John Doe");

        expectedDataNode.put("type", "TM01");
        expectedDataNode.put("date", "20110905053919");
        expectedDataNode.put("description", "Appointment Terminated, Director JOHN DOE");
        expectedDataNode.put("category", "2");

        // when
        final ObjectNode actualObjectNode = preTransformMapper.mapDeltaToObjectNode(delta);

        // then
        assertEquals(expectedObjectNode, actualObjectNode);
    }

    @Test
    void shouldMapDeltaObjectOntoObjectNodeWhenDeltaMissingFieldsAndNullDescriptionValues() {
        // given
        when(objectMapper.createObjectNode()).thenReturn(testObjectMapper.createObjectNode());

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

        final ObjectNode expectedObjectNode = testObjectMapper.createObjectNode();
        expectedObjectNode.put("company_number", "12345678");
        expectedObjectNode.put("_entity_id", "3063732185");

        expectedObjectNode.putObject("data")
                .put("type", "TM01")
                .put("date", "20110905053919")
                .put("description", "Appointment Terminated, Director JOHN DOE")
                .put("category", "2");

        // when
        final ObjectNode actualObjectNode = preTransformMapper.mapDeltaToObjectNode(delta);
        // then
        assertEquals(expectedObjectNode, actualObjectNode);
    }

    @Test
    void shouldMapDeltaObjectOntoObjectNodeWhenDeltaMissingFieldsAndEmptyDescriptionValuesObject() {
        // given
        when(objectMapper.createObjectNode()).thenReturn(testObjectMapper.createObjectNode());

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

        final ObjectNode expectedObjectNode = testObjectMapper.createObjectNode();
        expectedObjectNode.put("company_number", "12345678");
        expectedObjectNode.put("_entity_id", "3063732185");

        expectedObjectNode.putObject("data")
                .put("type", "TM01")
                .put("date", "20110905053919")
                .put("description", "Appointment Terminated, Director JOHN DOE")
                .put("category", "2");

        // when
        final ObjectNode actualObjectNode = preTransformMapper.mapDeltaToObjectNode(delta);

        // then
        assertEquals(expectedObjectNode, actualObjectNode);
    }
}
