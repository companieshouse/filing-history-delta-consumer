package uk.gov.companieshouse.filinghistory.consumer.delta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum.TOP_LEVEL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

    @InjectMocks
    private InternalFilingHistoryApiMapper mapper;

    @Mock
    private DescriptionValuesMapper descriptionValuesMapper;
    @Mock
    private LinksMapper linksMapper;
    @Mock
    private OriginalValuesMapper originalValuesMapper;

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
        when(linksMapper.map(any(), any())).thenReturn(filingHistoryItemDataLinks);
        when(originalValuesMapper.map(any())).thenReturn(internalDataOriginalValues);

        final JsonNode topLevelNode = buildJsonNode(BARCODE, DOCUMENT_ID, COMPANY_NUMBER);
        final JsonNode descriptionValuesNode = topLevelNode.get("data").get("description_values");
        final JsonNode originalValuesNode = topLevelNode.get("original_values");

        final InternalFilingHistoryApi expectedRequestBody = buildExpectedTM01RequestBody(BARCODE, DOCUMENT_ID, false);

        // when
        final InternalFilingHistoryApi actualRequestBody = mapper.mapJsonNodeToInternalFilingHistoryApi(topLevelNode, buildTransactionKindResult(ENCODED_ID), DELTA_AT, UPDATED_BY);

        // then
        assertEquals(expectedRequestBody, actualRequestBody);
        verify(descriptionValuesMapper).map(descriptionValuesNode);
        verify(linksMapper).map(COMPANY_NUMBER, ENCODED_ID);
        verify(originalValuesMapper).map(originalValuesNode);
    }

    @Test
    void shouldHandleNullInputFieldsBySettingNonRequiredFieldsToNullOnInternalFilingHistoryApiObject() {
        // given
        when(descriptionValuesMapper.map(any())).thenReturn(filingHistoryItemDataDescriptionValues);
        when(linksMapper.map(any(), any())).thenReturn(filingHistoryItemDataLinks);
        when(originalValuesMapper.map(any())).thenReturn(internalDataOriginalValues);


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

        // when
        final InternalFilingHistoryApi actualRequestBody = mapper.mapJsonNodeToInternalFilingHistoryApi(topLevelNode, buildTransactionKindResult(ENCODED_ID), DELTA_AT, UPDATED_BY);

        // then
        assertEquals(expectedRequestBody, actualRequestBody);
        verify(descriptionValuesMapper).map(descriptionValuesNode);
        verify(linksMapper).map(COMPANY_NUMBER, ENCODED_ID);
        verify(originalValuesMapper).map(originalValuesNode);
    }

    @Test
    void shouldMapTransformedJsonNodeToInternalFilingHistoryApiObjectWhenBarcodeDoesNotStartWithXAndSetPaperFiledToTrue() {
        // given
        when(descriptionValuesMapper.map(any())).thenReturn(filingHistoryItemDataDescriptionValues);
        when(linksMapper.map(any(), any())).thenReturn(filingHistoryItemDataLinks);
        when(originalValuesMapper.map(any())).thenReturn(internalDataOriginalValues);

        final JsonNode topLevelNode = buildJsonNode("TAITVXAX", "000TAITVXAX4682", COMPANY_NUMBER);
        final JsonNode descriptionValuesNode = topLevelNode.get("data").get("description_values");
        final JsonNode originalValuesNode = topLevelNode.get("original_values");

        final InternalFilingHistoryApi expectedRequestBody = buildExpectedTM01RequestBody("TAITVXAX", "000TAITVXAX4682", true);

        // when
        final InternalFilingHistoryApi actualRequestBody = mapper.mapJsonNodeToInternalFilingHistoryApi(topLevelNode, buildTransactionKindResult(ENCODED_ID), DELTA_AT, UPDATED_BY);

        // then
        assertEquals(expectedRequestBody, actualRequestBody);
        verify(descriptionValuesMapper).map(descriptionValuesNode);
        verify(linksMapper).map(COMPANY_NUMBER, ENCODED_ID);
        verify(originalValuesMapper).map(originalValuesNode);
    }

    @ParameterizedTest
    @CsvSource({
            "000XAITVXAX4682 , false",
            "000TAITVXAX4682 , true"})
    void shouldMapTransformedJsonNodeToInternalFilingHistoryApiObjectWhenBarcodeIsEmptyButDocumentIdIsNot(final String documentId, final boolean isPaperFiled) {
        // given
        when(descriptionValuesMapper.map(any())).thenReturn(filingHistoryItemDataDescriptionValues);
        when(linksMapper.map(any(), any())).thenReturn(filingHistoryItemDataLinks);
        when(originalValuesMapper.map(any())).thenReturn(internalDataOriginalValues);

        final JsonNode topLevelNode = buildJsonNode("", documentId, COMPANY_NUMBER);
        final JsonNode descriptionValuesNode = topLevelNode.get("data").get("description_values");
        final JsonNode originalValuesNode = topLevelNode.get("original_values");

        final InternalFilingHistoryApi expectedRequestBody = buildExpectedTM01RequestBody("", documentId, isPaperFiled);

        // when
        final InternalFilingHistoryApi actualRequestBody = mapper.mapJsonNodeToInternalFilingHistoryApi(topLevelNode, buildTransactionKindResult(ENCODED_ID), DELTA_AT, UPDATED_BY);

        // then
        assertEquals(expectedRequestBody, actualRequestBody);
        verify(descriptionValuesMapper).map(descriptionValuesNode);
        verify(linksMapper).map(COMPANY_NUMBER, ENCODED_ID);
        verify(originalValuesMapper).map(originalValuesNode);
    }

    @ParameterizedTest
    @CsvSource({
            "12345678 , ",
            " , MzA1Njc0Mjg0N3NqYXNqamQ"
    })
    void shouldThrowIllegalArgumentExceptionWhenNullOrEmptyCompanyNumberOrTransactionId(final String companyNumber, final String transactionId) {
        // given
        when(descriptionValuesMapper.map(any())).thenReturn(filingHistoryItemDataDescriptionValues);
        when(linksMapper.map(any(), any())).thenThrow(new IllegalArgumentException());

        final JsonNode topLevelNode = buildJsonNode(BARCODE, DOCUMENT_ID, companyNumber);
        final JsonNode descriptionValuesNode = topLevelNode.get("data").get("description_values");

        // when
        Executable executable = () -> mapper.mapJsonNodeToInternalFilingHistoryApi(topLevelNode, buildTransactionKindResult(transactionId), DELTA_AT, UPDATED_BY);

        // then
        assertThrows(IllegalArgumentException.class, executable);
        verify(descriptionValuesMapper).map(descriptionValuesNode);
        verify(linksMapper).map(companyNumber, transactionId);
        verifyNoInteractions(originalValuesMapper);
    }

    private static JsonNode buildJsonNode(final String barcode, final String documentId, final String companyNumber) {
        final ObjectMapper objectMapper =
                new ObjectMapper()
                        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                        .registerModule(new JavaTimeModule());

        ObjectNode topLevelNode = objectMapper.createObjectNode()
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
        final ObjectMapper objectMapper =
                new ObjectMapper()
                        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                        .registerModule(new JavaTimeModule());

        ObjectNode topLevelNode = objectMapper.createObjectNode()
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

    private InternalFilingHistoryApi buildExpectedTM01RequestBody(final String barcode, final String documentId, final boolean isPaperFiled) {
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
