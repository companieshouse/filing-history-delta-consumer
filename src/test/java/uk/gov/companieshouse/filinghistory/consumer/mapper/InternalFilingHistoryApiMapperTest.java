package uk.gov.companieshouse.filinghistory.consumer.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum.TOP_LEVEL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataLinks;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalDataOriginalValues;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.consumer.mapper.DescriptionValuesMapper;
import uk.gov.companieshouse.filinghistory.consumer.mapper.InternalFilingHistoryApiMapper;
import uk.gov.companieshouse.filinghistory.consumer.mapper.InternalFilingHistoryApiMapperArguments;
import uk.gov.companieshouse.filinghistory.consumer.mapper.LinksMapper;
import uk.gov.companieshouse.filinghistory.consumer.mapper.OriginalValuesMapper;
import uk.gov.companieshouse.filinghistory.consumer.mapper.PaperFiledMapper;
import uk.gov.companieshouse.filinghistory.consumer.service.TransactionKindResult;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.TransformerTestingUtils;

@ExtendWith(MockitoExtension.class)
class InternalFilingHistoryApiMapperTest {

    private static final String DELTA_AT = "20140815230459600643";
    private static final String ENTITY_ID = "3056742847";
    private static final String ENCODED_ID = "MzA1Njc0Mjg0N3NqYXNqamQ";
    private static final String BARCODE = "XAITVXAX";
    private static final String DESCRIPTION = "termination-director-company-with-name-termination-date";
    private static final String ORIGINAL_DESCRIPTION = "Appointment terminated, director john tester";
    private static final String DOCUMENT_ID = "000%s4682".formatted(BARCODE);
    private static final String COMPANY_NUMBER = "12345678";
    private static final String DATE = "20110905053919";
    private static final String TYPE = "TM01";
    private static final String CATEGORY = "officers";
    private static final String SUBCATEGORY = "termination";
    private static final String UPDATED_BY = "updatedBy";
    private static final ObjectMapper MAPPER = TransformerTestingUtils.getMapper();

    @InjectMocks
    private InternalFilingHistoryApiMapper mapper;

    @Mock
    private DescriptionValuesMapper descriptionValuesMapper;
    @Mock
    private LinksMapper linksMapper;
    @Mock
    private OriginalValuesMapper originalValuesMapper;
    @Mock
    private PaperFiledMapper paperFiledMapper;

    @Mock
    private FilingHistoryItemDataDescriptionValues filingHistoryItemDataDescriptionValues;
    @Mock
    private FilingHistoryItemDataLinks filingHistoryItemDataLinks;
    @Mock
    private InternalDataOriginalValues internalDataOriginalValues;


    @Test
    void shouldMapTransformedJsonNodeToInternalFilingHistoryApiObject() {
        // given
        when(descriptionValuesMapper.map(any())).thenReturn(filingHistoryItemDataDescriptionValues);
        when(originalValuesMapper.map(any())).thenReturn(internalDataOriginalValues);
        when(paperFiledMapper.map(any(), any())).thenReturn(false);
        when(linksMapper.map(any(), any())).thenReturn(filingHistoryItemDataLinks);

        final JsonNode topLevelNode = buildJsonNode(BARCODE, DOCUMENT_ID, COMPANY_NUMBER);
        final JsonNode descriptionValuesNode = topLevelNode.get("data").get("description_values");
        final JsonNode originalValuesNode = topLevelNode.get("original_values");

        final InternalFilingHistoryApi expectedRequestBody = buildExpectedTM01RequestBody(BARCODE, DOCUMENT_ID, false);
        InternalFilingHistoryApiMapperArguments arguments = new InternalFilingHistoryApiMapperArguments(
                topLevelNode,
                buildTransactionKindResult(ENCODED_ID),
                COMPANY_NUMBER,
                DELTA_AT,
                UPDATED_BY);

        // when
        final InternalFilingHistoryApi actualRequestBody = mapper.mapJsonNodeToInternalFilingHistoryApi(arguments);

        // then
        assertEquals(expectedRequestBody, actualRequestBody);
        verify(descriptionValuesMapper).map(descriptionValuesNode);
        verify(originalValuesMapper).map(originalValuesNode);
        verify(paperFiledMapper).map(BARCODE, DOCUMENT_ID);
        verify(linksMapper).map(COMPANY_NUMBER, ENCODED_ID);
    }

    @Test
    void shouldHandleNullInputFieldsBySettingNonRequiredFieldsToNullOnInternalFilingHistoryApiObject() {
        // given
        when(descriptionValuesMapper.map(any())).thenReturn(filingHistoryItemDataDescriptionValues);
        when(originalValuesMapper.map(any())).thenReturn(internalDataOriginalValues);
        when(paperFiledMapper.map(any(), any())).thenReturn(false);
        when(linksMapper.map(any(), any())).thenReturn(filingHistoryItemDataLinks);

        final JsonNode topLevelNode = buildJsonNodeWithNoNonRequiredFields();
        final JsonNode descriptionValuesNode = topLevelNode.get("data").get("description_values");
        final JsonNode originalValuesNode = topLevelNode.get("original_values");

        final InternalFilingHistoryApi expectedRequestBody = new InternalFilingHistoryApi()
                .externalData(new ExternalData()
                        .transactionId(ENCODED_ID)
                        .barcode(null)
                        .type(null)
                        .date(null)
                        .category(null)
                        .subcategory(null)
                        .description(null)
                        .descriptionValues(filingHistoryItemDataDescriptionValues)
                        .actionDate(null)
                        .links(filingHistoryItemDataLinks)
                        .paperFiled(false))
                .internalData(new InternalData()
                        .transactionKind(TOP_LEVEL)
                        .deltaAt(DELTA_AT)
                        .originalValues(internalDataOriginalValues)
                        .originalDescription(null)
                        .companyNumber(COMPANY_NUMBER)
                        .parentEntityId(null)
                        .entityId(ENTITY_ID)
                        .documentId(null)
                        .updatedBy(UPDATED_BY));

        InternalFilingHistoryApiMapperArguments arguments = new InternalFilingHistoryApiMapperArguments(
                topLevelNode,
                buildTransactionKindResult(ENCODED_ID),
                COMPANY_NUMBER,
                DELTA_AT,
                UPDATED_BY);

        // when
        final InternalFilingHistoryApi actualRequestBody = mapper.mapJsonNodeToInternalFilingHistoryApi(arguments);

        // then
        assertEquals(expectedRequestBody, actualRequestBody);
        verify(descriptionValuesMapper).map(descriptionValuesNode);
        verify(originalValuesMapper).map(originalValuesNode);
        verify(paperFiledMapper).map(null, null);
        verify(linksMapper).map(COMPANY_NUMBER, ENCODED_ID);
    }

    @Test
    void shouldMapTransformedJsonNodeToInternalFilingHistoryApiObjectWhenBarcodeDoesNotStartWithXAndSetPaperFiledToTrue() {
        // given
        when(descriptionValuesMapper.map(any())).thenReturn(filingHistoryItemDataDescriptionValues);
        when(originalValuesMapper.map(any())).thenReturn(internalDataOriginalValues);
        when(paperFiledMapper.map(any(), any())).thenReturn(true);
        when(linksMapper.map(any(), any())).thenReturn(filingHistoryItemDataLinks);

        final JsonNode topLevelNode = buildJsonNode("TAITVXAX", "000TAITVXAX4682", COMPANY_NUMBER);
        final JsonNode descriptionValuesNode = topLevelNode.get("data").get("description_values");
        final JsonNode originalValuesNode = topLevelNode.get("original_values");

        final InternalFilingHistoryApi expectedRequestBody = buildExpectedTM01RequestBody("TAITVXAX", "000TAITVXAX4682",
                true);
        InternalFilingHistoryApiMapperArguments arguments = new InternalFilingHistoryApiMapperArguments(
                topLevelNode,
                buildTransactionKindResult(ENCODED_ID),
                COMPANY_NUMBER,
                DELTA_AT,
                UPDATED_BY);

        // when
        final InternalFilingHistoryApi actualRequestBody = mapper.mapJsonNodeToInternalFilingHistoryApi(arguments);

        // then
        assertEquals(expectedRequestBody, actualRequestBody);
        verify(descriptionValuesMapper).map(descriptionValuesNode);
        verify(originalValuesMapper).map(originalValuesNode);
        verify(paperFiledMapper).map("TAITVXAX", "000TAITVXAX4682");
        verify(linksMapper).map(COMPANY_NUMBER, ENCODED_ID);
    }

    @ParameterizedTest
    @CsvSource({
            "000XAITVXAX4682 , false",
            "000TAITVXAX4682 , true"})
    void shouldMapTransformedJsonNodeToInternalFilingHistoryApiObjectWhenBarcodeIsEmptyButDocumentIdIsNot(
            final String documentId, final boolean isPaperFiled) {
        // given
        when(descriptionValuesMapper.map(any())).thenReturn(filingHistoryItemDataDescriptionValues);
        when(originalValuesMapper.map(any())).thenReturn(internalDataOriginalValues);
        when(paperFiledMapper.map(any(), any())).thenReturn(isPaperFiled);
        when(linksMapper.map(any(), any())).thenReturn(filingHistoryItemDataLinks);

        final JsonNode topLevelNode = buildJsonNode("", documentId, COMPANY_NUMBER);
        final JsonNode descriptionValuesNode = topLevelNode.get("data").get("description_values");
        final JsonNode originalValuesNode = topLevelNode.get("original_values");

        final InternalFilingHistoryApi expectedRequestBody = buildExpectedTM01RequestBody("", documentId, isPaperFiled);
        InternalFilingHistoryApiMapperArguments arguments = new InternalFilingHistoryApiMapperArguments(
                topLevelNode,
                buildTransactionKindResult(ENCODED_ID),
                COMPANY_NUMBER,
                DELTA_AT,
                UPDATED_BY);

        // when
        final InternalFilingHistoryApi actualRequestBody = mapper.mapJsonNodeToInternalFilingHistoryApi(arguments);

        // then
        assertEquals(expectedRequestBody, actualRequestBody);
        verify(descriptionValuesMapper).map(descriptionValuesNode);
        verify(originalValuesMapper).map(originalValuesNode);
        verify(paperFiledMapper).map("", documentId);
        verify(linksMapper).map(COMPANY_NUMBER, ENCODED_ID);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "12345678 , null",
            "null , MzA1Njc0Mjg0N3NqYXNqamQ",
            "null , null"
    },
            nullValues = {"null"})
    void shouldThrowIllegalArgumentExceptionWhenNullOrEmptyCompanyNumberOrTransactionId(final String companyNumber,
            final String transactionId) {
        // given
        when(descriptionValuesMapper.map(any())).thenReturn(filingHistoryItemDataDescriptionValues);
        when(paperFiledMapper.map(any(), any())).thenReturn(false);
        when(linksMapper.map(any(), any())).thenThrow(IllegalArgumentException.class);

        final JsonNode topLevelNode = buildJsonNode(BARCODE, DOCUMENT_ID, companyNumber);
        InternalFilingHistoryApiMapperArguments arguments = new InternalFilingHistoryApiMapperArguments(
                topLevelNode,
                buildTransactionKindResult(transactionId),
                companyNumber,
                DELTA_AT,
                UPDATED_BY);

        // when
        Executable executable = () -> mapper.mapJsonNodeToInternalFilingHistoryApi(arguments);

        // then
        assertThrows(IllegalArgumentException.class, executable);
        verify(descriptionValuesMapper).map(topLevelNode.get("data").get("description_values"));
        verify(paperFiledMapper).map(BARCODE, DOCUMENT_ID);
        verify(linksMapper).map(companyNumber, transactionId);
        verifyNoInteractions(originalValuesMapper);
    }

    @Test
    void shouldMapTransformedJsonNodeToInternalFilingHistoryApiObjectWithNullFieldsWhenJsonNodeObjectsAreNull() {
        // given
        when(paperFiledMapper.map(any(), any())).thenReturn(true);
        when(linksMapper.map(any(), any())).thenReturn(filingHistoryItemDataLinks);

        final InternalFilingHistoryApi expectedRequestBody = new InternalFilingHistoryApi()
                .externalData(new ExternalData()
                        .transactionId(ENCODED_ID)
                        .barcode(null)
                        .type(null)
                        .date(null)
                        .category(null)
                        .subcategory(null)
                        .description(null)
                        .descriptionValues(null)
                        .actionDate(null)
                        .links(filingHistoryItemDataLinks)
                        .paperFiled(true))
                .internalData(new InternalData()
                        .transactionKind(TOP_LEVEL)
                        .deltaAt(DELTA_AT)
                        .originalValues(null)
                        .originalDescription(null)
                        .companyNumber(COMPANY_NUMBER)
                        .parentEntityId(null)
                        .entityId(null)
                        .documentId(null)
                        .updatedBy(UPDATED_BY));

        InternalFilingHistoryApiMapperArguments arguments = new InternalFilingHistoryApiMapperArguments(
                null,
                buildTransactionKindResult(ENCODED_ID),
                COMPANY_NUMBER,
                DELTA_AT,
                UPDATED_BY);

        // when
        final InternalFilingHistoryApi actualRequestBody = mapper.mapJsonNodeToInternalFilingHistoryApi(arguments);

        // then
        assertEquals(expectedRequestBody, actualRequestBody);
        verify(descriptionValuesMapper).map(null);
        verify(originalValuesMapper).map(null);
        verify(paperFiledMapper).map(null, null);
        verify(linksMapper).map(COMPANY_NUMBER, ENCODED_ID);
    }

    private static JsonNode buildJsonNode(final String barcode, final String documentId, final String companyNumber) {
        ObjectNode topLevelNode = MAPPER.createObjectNode()
                .put("_barcode", barcode)
                .put("original_description", ORIGINAL_DESCRIPTION)
                .put("parent_entity_id", "")
                .put("_entity_id", ENTITY_ID)
                .put("_document_id", documentId)
                .put("company_number", companyNumber);

        topLevelNode.putObject("original_values");

        topLevelNode.putObject("data")
                .put("type", TYPE)
                .put("date", DATE)
                .put("category", CATEGORY)
                .put("subcategory", SUBCATEGORY)
                .put("description", DESCRIPTION)
                .put("action_date", DATE)
                .putObject("description_values");

        return topLevelNode;
    }

    private static JsonNode buildJsonNodeWithNoNonRequiredFields() {
        ObjectNode topLevelNode = MAPPER.createObjectNode()
                .put("_entity_id", ENTITY_ID)
                .put("company_number", COMPANY_NUMBER);

        topLevelNode.putObject("original_values");

        topLevelNode.putObject("data")
                .putObject("description_values");

        return topLevelNode;
    }

    private static TransactionKindResult buildTransactionKindResult(final String transactionId) {
        return new TransactionKindResult(transactionId, TOP_LEVEL);
    }

    private InternalFilingHistoryApi buildExpectedTM01RequestBody(final String barcode, final String documentId,
            final boolean isPaperFiled) {
        return new InternalFilingHistoryApi()
                .externalData(new ExternalData()
                        .transactionId(ENCODED_ID)
                        .barcode(barcode)
                        .type(TYPE)
                        .date(DATE)
                        .category(ExternalData.CategoryEnum.OFFICERS)
                        .subcategory(ExternalData.SubcategoryEnum.TERMINATION)
                        .description(DESCRIPTION)
                        .descriptionValues(filingHistoryItemDataDescriptionValues)
                        .actionDate(DATE)
                        .links(filingHistoryItemDataLinks)
                        .paperFiled(isPaperFiled))
                .internalData(new InternalData()
                        .transactionKind(TOP_LEVEL)
                        .deltaAt(DELTA_AT)
                        .originalValues(internalDataOriginalValues)
                        .originalDescription(ORIGINAL_DESCRIPTION)
                        .companyNumber(COMPANY_NUMBER)
                        .parentEntityId("")
                        .entityId(ENTITY_ID)
                        .documentId(documentId)
                        .updatedBy(UPDATED_BY));
    }
}