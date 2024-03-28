package uk.gov.companieshouse.filinghistory.consumer.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
import uk.gov.companieshouse.api.filinghistory.DescriptionValues;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.ExternalData.CategoryEnum;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalDataOriginalValues;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.api.filinghistory.Links;
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
    private SubcategoryMapper subcategoryMapper;
    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private DescriptionValuesMapper descriptionValuesMapper;
    @Mock
    private LinksMapper linksMapper;
    @Mock
    private OriginalValuesMapper originalValuesMapper;
    @Mock
    private PaperFiledMapper paperFiledMapper;

    @Mock
    private Object subcategory;
    @Mock
    private DescriptionValues DescriptionValues;
    @Mock
    private Links Links;
    @Mock
    private InternalDataOriginalValues internalDataOriginalValues;


    @Test
    void shouldMapTransformedJsonNodeToInternalFilingHistoryApiObject() {
        // given
        when(categoryMapper.map(any())).thenReturn(CategoryEnum.OFFICERS);
        when(subcategoryMapper.map(any())).thenReturn(subcategory);
        when(descriptionValuesMapper.map(any())).thenReturn(DescriptionValues);
        when(originalValuesMapper.map(any())).thenReturn(internalDataOriginalValues);
        when(paperFiledMapper.isPaperFiled(any(), any())).thenReturn(false);
        when(linksMapper.map(any(), any())).thenReturn(Links);

        final JsonNode topLevelNode = buildJsonNode(BARCODE, DOCUMENT_ID, COMPANY_NUMBER);
        final JsonNode dataNode = topLevelNode.get("data");
        final JsonNode descriptionValuesNode = dataNode.get("description_values");
        final JsonNode originalValuesNode = topLevelNode.get("original_values");

        final InternalFilingHistoryApi expectedRequestBody = buildExpectedTM01RequestBody(BARCODE, DOCUMENT_ID, null);
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
        verify(originalValuesMapper).map(originalValuesNode);
        verify(subcategoryMapper).map(dataNode);
        verify(categoryMapper).map(dataNode);
        verify(descriptionValuesMapper).map(descriptionValuesNode);
        verify(paperFiledMapper).isPaperFiled(BARCODE, DOCUMENT_ID);
        verify(linksMapper).map(COMPANY_NUMBER, ENCODED_ID);
    }

    @Test
    void shouldHandleNullInputFieldsBySettingNonRequiredFieldsToNullOnInternalFilingHistoryApiObject() {
        // given
        when(categoryMapper.map(any())).thenReturn(null);
        when(subcategoryMapper.map(any())).thenReturn(subcategory);
        when(descriptionValuesMapper.map(any())).thenReturn(DescriptionValues);
        when(originalValuesMapper.map(any())).thenReturn(internalDataOriginalValues);
        when(paperFiledMapper.isPaperFiled(any(), any())).thenReturn(false);
        when(linksMapper.map(any(), any())).thenReturn(Links);

        final JsonNode topLevelNode = buildJsonNodeWithNoNonRequiredFields();
        final JsonNode dataNode = topLevelNode.get("data");
        final JsonNode descriptionValuesNode = dataNode.get("description_values");
        final JsonNode originalValuesNode = topLevelNode.get("original_values");

        final InternalFilingHistoryApi expectedRequestBody = new InternalFilingHistoryApi()
                .externalData(new ExternalData()
                        .transactionId(ENCODED_ID)
                        .barcode(null)
                        .type(null)
                        .date(null)
                        .category(null)
                        .subcategory(subcategory)
                        .description(null)
                        .descriptionValues(DescriptionValues)
                        .actionDate(null)
                        .links(Links)
                        .paperFiled(null))
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
        verify(originalValuesMapper).map(originalValuesNode);
        verify(categoryMapper).map(dataNode);
        verify(subcategoryMapper).map(dataNode);
        verify(descriptionValuesMapper).map(descriptionValuesNode);
        verify(paperFiledMapper).isPaperFiled(null, null);
        verify(linksMapper).map(COMPANY_NUMBER, ENCODED_ID);
    }

    @Test
    void shouldMapTransformedJsonNodeToInternalFilingHistoryApiObjectWhenBarcodeDoesNotStartWithXAndSetPaperFiledToTrue() {
        // given
        when(categoryMapper.map(any())).thenReturn(CategoryEnum.OFFICERS);
        when(subcategoryMapper.map(any())).thenReturn(subcategory);
        when(descriptionValuesMapper.map(any())).thenReturn(DescriptionValues);
        when(originalValuesMapper.map(any())).thenReturn(internalDataOriginalValues);
        when(paperFiledMapper.isPaperFiled(any(), any())).thenReturn(true);
        when(linksMapper.map(any(), any())).thenReturn(Links);

        final JsonNode topLevelNode = buildJsonNode("TAITVXAX", "000TAITVXAX4682", COMPANY_NUMBER);
        final JsonNode dataNode = topLevelNode.get("data");
        final JsonNode descriptionValuesNode = dataNode.get("description_values");
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
        verify(originalValuesMapper).map(originalValuesNode);
        verify(categoryMapper).map(dataNode);
        verify(subcategoryMapper).map(dataNode);
        verify(descriptionValuesMapper).map(descriptionValuesNode);
        verify(paperFiledMapper).isPaperFiled("TAITVXAX", "000TAITVXAX4682");
        verify(linksMapper).map(COMPANY_NUMBER, ENCODED_ID);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "000XAITVXAX4682 , false",
            "000TAITVXAX4682 , true"},
            nullValues = {"null"})
    void shouldMapTransformedJsonNodeToInternalFilingHistoryApiObjectWhenBarcodeIsEmptyButDocumentIdIsNot(
            final String documentId, final Boolean isPaperFiled) {
        // given
        when(categoryMapper.map(any())).thenReturn(CategoryEnum.OFFICERS);
        when(subcategoryMapper.map(any())).thenReturn(subcategory);
        when(descriptionValuesMapper.map(any())).thenReturn(DescriptionValues);
        when(originalValuesMapper.map(any())).thenReturn(internalDataOriginalValues);
        when(paperFiledMapper.isPaperFiled(any(), any())).thenReturn(isPaperFiled);
        when(linksMapper.map(any(), any())).thenReturn(Links);

        final JsonNode topLevelNode = buildJsonNode("", documentId, COMPANY_NUMBER);
        final JsonNode dataNode = topLevelNode.get("data");
        final JsonNode descriptionValuesNode = dataNode.get("description_values");
        final JsonNode originalValuesNode = topLevelNode.get("original_values");

        final InternalFilingHistoryApi expectedRequestBody = buildExpectedTM01RequestBody("", documentId,
                isPaperFiled ? true : null);
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
        verify(originalValuesMapper).map(originalValuesNode);
        verify(categoryMapper).map(dataNode);
        verify(subcategoryMapper).map(dataNode);
        verify(descriptionValuesMapper).map(descriptionValuesNode);
        verify(paperFiledMapper).isPaperFiled("", documentId);
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
        when(categoryMapper.map(any())).thenReturn(CategoryEnum.OFFICERS);
        when(subcategoryMapper.map(any())).thenReturn(subcategory);
        when(descriptionValuesMapper.map(any())).thenReturn(DescriptionValues);
        when(paperFiledMapper.isPaperFiled(any(), any())).thenReturn(false);
        when(linksMapper.map(any(), any())).thenThrow(IllegalArgumentException.class);

        final JsonNode topLevelNode = buildJsonNode(BARCODE, DOCUMENT_ID, companyNumber);
        final JsonNode dataNode = topLevelNode.get("data");
        final JsonNode descriptionValuesNode = dataNode.get("description_values");
        final JsonNode originalValuesNode = topLevelNode.get("original_values");

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
        verify(originalValuesMapper).map(originalValuesNode);
        verify(categoryMapper).map(dataNode);
        verify(subcategoryMapper).map(dataNode);
        verify(descriptionValuesMapper).map(descriptionValuesNode);
        verify(paperFiledMapper).isPaperFiled(BARCODE, DOCUMENT_ID);
        verify(linksMapper).map(companyNumber, transactionId);
    }

    @Test
    void shouldMapTransformedJsonNodeToInternalFilingHistoryApiObjectWithNullFieldsWhenJsonNodeObjectsAreNull() {
        // given
        when(paperFiledMapper.isPaperFiled(any(), any())).thenReturn(true);
        when(linksMapper.map(any(), any())).thenReturn(Links);

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
                        .links(Links)
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
        verify(paperFiledMapper).isPaperFiled(null, null);
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
                                                                  final Boolean isPaperFiled) {
        return new InternalFilingHistoryApi()
                .externalData(new ExternalData()
                        .transactionId(ENCODED_ID)
                        .barcode(barcode)
                        .type(TYPE)
                        .date(DATE)
                        .category(CategoryEnum.OFFICERS)
                        .subcategory(subcategory)
                        .description(DESCRIPTION)
                        .descriptionValues(DescriptionValues)
                        .actionDate(DATE)
                        .links(Links)
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
