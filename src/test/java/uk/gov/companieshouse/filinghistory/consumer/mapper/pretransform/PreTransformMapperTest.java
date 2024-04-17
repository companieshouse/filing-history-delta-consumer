package uk.gov.companieshouse.filinghistory.consumer.mapper.pretransform;

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
import uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.TransformerTestingUtils;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.FormatDate;

@ExtendWith(MockitoExtension.class)
class PreTransformMapperTest {

    private static final ObjectMapper MAPPER = TransformerTestingUtils.getMapper();

    private PreTransformMapper preTransformMapper;
    @Mock
    private FormatDate formatDate;
    @Mock
    private ChildNodeMapperFactory childNodeMapperFactory;
    @Mock
    private AnnotationNodeMapper annotationNodeMapper;
    @Mock
    private ChildPair childPair;

    @BeforeEach
    void setUp() {
        preTransformMapper = new PreTransformMapper(MAPPER, formatDate, childNodeMapperFactory);
    }

    @Test
    void shouldDelegateToChildNodeMapperFromFactory() {
        // given
        final FilingHistory delta = new FilingHistory();

        when(childNodeMapperFactory.getChildMapper(any())).thenReturn(annotationNodeMapper);
        when(annotationNodeMapper.mapChildObjectNode(any())).thenReturn(childPair);

        // when
        final ChildPair actual = preTransformMapper.mapChildDeltaToObjectNode(TransactionKindEnum.ANNOTATION, delta);

        // then
        assertEquals(childPair, actual);
        verify(childNodeMapperFactory).getChildMapper(TransactionKindEnum.ANNOTATION);
        verify(annotationNodeMapper).mapChildObjectNode(delta);
    }

    @Test
    void shouldMapDeltaObjectOntoObjectNode() {
        // given
        final FilingHistoryDelta delta = getFilingHistoryDelta(new DescriptionValues()
                .accType("small")
                .accountingPeriod("10 days")
                .action("action")
                .appointmentDate("01/01/2010")
                .capitalType("statement")
                .caseStartDate("04/07/2011")
                .caseEndDate("06/05/2013")
                .cessationDate("05/06/2013")
                .changeDate("04/04/2013")
                .chargeCreationDate("05/05/2014")
                .madeUpDate("09/09/2018")
                .mortgageSatisfactionDate("20/10/2005")
                .newRoAddress("5 Test Road")
                .newDate("10/10/2019")
                .notificationDate("11/11/2020")
                .officerName("John Doe")
                .periodType("weeks")
                .propertyAcquiredDate("12/12/2021")
                .pscName("Significant Person")
                .resignationDate("03/02/2013"));

        final JsonNode expectedTopLevelNode = getExpectedJsonNode(MAPPER.createObjectNode()
                .put("acc_type", "small")
                .put("accounting_period", "10 days")
                .put("action", "action")
                .put("appointment_date", "01/01/2010")
                .put("capital_type", "statement")
                .put("case_start_date", "04/07/2011")
                .put("case_end_date", "06/05/2013")
                .put("cessation_date", "05/06/2013")
                .put("change_date", "04/04/2013")
                .put("charge_creation_date", "05/05/2014")
                .put("made_up_date", "09/09/2018")
                .put("mortgage_satisfaction_date", "20/10/2005")
                .put("new_ro_address", "5 Test Road")
                .put("new_date", "10/10/2019")
                .put("notification_date", "11/11/2020")
                .put("officer_name", "John Doe")
                .put("period_type", "weeks")
                .put("property_acquired_date", "12/12/2021")
                .put("psc_name", "Significant Person")
                .put("resignation_date", "03/02/2013"));

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
    void shouldMapDeltaObjectOntoObjectNodeDescriptionWithAngledBrackets() {
        // given
        final FilingHistoryDelta delta = new FilingHistoryDelta()
                .deltaAt("20140916230459600643")
                .filingHistory(List.of(
                        new FilingHistory()
                                .category("2")
                                .receiveDate("20110905053919")
                                .formType("TM01")
                                .description("REGISTERED OFFICE CHANGED \n ON 04/05/88 FROM:< 35 KENT HOUSE \n LANE  BECKENHAM  KENT  BR3 1LE")
                                .barcode("")
                                .documentId("")
                                .descriptionValues(null)
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
                .putObject("data")
                .put("type", "TM01")
                .put("date", "2011-09-05T05:39:19Z")
                .put("description", "REGISTERED OFFICE CHANGED \\ ON 04/05/88 FROM:\\ 35 KENT HOUSE \\ LANE  BECKENHAM  KENT  BR3 1LE")
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

        return expectedTopLevelNode;
    }
}
