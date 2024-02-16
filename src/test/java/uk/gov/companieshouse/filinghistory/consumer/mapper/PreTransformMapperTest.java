package uk.gov.companieshouse.filinghistory.consumer.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.DescriptionValues;
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.api.delta.FilingHistoryDelta;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.TransformerTestingUtils;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.FormatDate;

@ExtendWith(MockitoExtension.class)
class PreTransformMapperTest {

    private static final ObjectMapper MAPPER = TransformerTestingUtils.getMapper();

    private PreTransformMapper preTransformMapper;
    @Mock
    private FormatDate formatDate;

    @BeforeEach
    void setUp() {
        preTransformMapper = new PreTransformMapper(MAPPER, formatDate);
    }

    @Test
    void shouldMapDeltaObjectOntoObjectNode() {
        // given
        final FilingHistoryDelta delta = getFilingHistoryDelta(new DescriptionValues()
                .resignationDate("02/07/2011")
                .OFFICER_NAME("John Doe")
                .cessationDate("01/03/2012")
                .changeDate("05/06/2013")
                .notificationDate("10/11/2014")
                .pscName("Significant Person")
                .accType("Small")
                .caseEndDate("21/02/2011")
                .madeUpDate("30/12/2020")
                .newRoAddress("11 Test Lane")
                .appointmentDate("01/02/2013")
                .chargeCreationDate("20/10/2010")
                .propertyAcquiredDate("07/07/2017")
                .notificationDate("18/05/2009")
                .accountingPeriod("10 days")
                .periodType("days")
                .newDate("20/10/2010"));

        final JsonNode expectedTopLevelNode = getExpectedJsonNode(MAPPER.createObjectNode()
                .put("resignation_date", "02/07/2011")
                .put("officer_name", "John Doe")
                .put("cessation_date", "01/03/2012")
                .put("change_date", "05/06/2013")
                .put("notification_date", "10/11/2014")
                .put("psc_name", "Significant Person")
                .put("acc_type", "Small")
                .put("case_end_date", "21/02/2011")
                .put("made_up_date", "30/12/2020")
                .put("new_ro_address", "11 Test Lane")
                .put("appointment_date", "01/02/2013")
                .put("charge_creation_date", "20/10/2010")
                .put("property_acquired_date", "07/07/2017")
                .put("notification_date", "18/05/2009")
                .put("accounting_period", "10 days")
                .put("period_type", "days")
                .put("new_date", "20/10/2010"));

        when(formatDate.format(any())).thenReturn("2011-09-05T05:39:19Z");

        // when
        final ObjectNode actualObjectNode = preTransformMapper.mapDeltaToObjectNode(
                delta.getFilingHistory().getFirst());

        // then
        assertEquals(expectedTopLevelNode, actualObjectNode);
        verify(formatDate).format("20110905053919");
    }


    @Test
    void shouldMapDeltaObjectOntoObjectNodeLowerCaseOfficerName() {
        // given
        final FilingHistoryDelta delta = getFilingHistoryDelta(new DescriptionValues()
                .resignationDate("02/07/2011")
                .officerName("John Doe"));

        final JsonNode expectedTopLevelNode = getExpectedJsonNode(MAPPER.createObjectNode()
                .put("resignation_date", "02/07/2011")
                .put("officer_name", "John Doe"));

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
        final FilingHistoryDelta delta = getFilingHistoryDelta(null);

        final JsonNode expectedTopLevelNode = getExpectedJsonNode(null);

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

        final ObjectNode expectedTopLevelNode = MAPPER.createObjectNode()
                .put("company_number", "12345678")
                .put("_entity_id", "3063732185")
                .put("parent_entity_id", "")
                .put("parent_form_type", "")
                .put("pre_scanned_batch", "0");

        expectedTopLevelNode
                .putObject("original_values");

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

    private static FilingHistoryDelta getFilingHistoryDelta(DescriptionValues descriptionValues) {
        return new FilingHistoryDelta()
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
                                .descriptionValues(descriptionValues)
                                .preScannedBatch("0")
                ));
    }

    private static JsonNode getExpectedJsonNode(ObjectNode originalValues) {
        ObjectNode expectedTopLevelNode = MAPPER.createObjectNode()
                .put("company_number", "12345678")
                .put("_entity_id", "3063732185")
                .put("_barcode", "XAITVXAX")
                .put("_document_id", "000XAITVXAX4682")
                .put("parent_entity_id", "")
                .put("parent_form_type", "")
                .put("pre_scanned_batch", "0");

        if (originalValues != null) {
            expectedTopLevelNode
                    .putIfAbsent("original_values", originalValues);
        }

        expectedTopLevelNode
                .putObject("data")
                .put("type", "TM01")
                .put("date", "2011-09-05T05:39:19Z")
                .put("description", "Appointment Terminated, Director JOHN DOE")
                .put("category", "2");

        return  expectedTopLevelNode;
    }
}
