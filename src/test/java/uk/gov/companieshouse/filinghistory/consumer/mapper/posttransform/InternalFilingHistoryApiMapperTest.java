package uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.InternalData;
import uk.gov.companieshouse.api.filinghistory.InternalDataOriginalValues;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;
import uk.gov.companieshouse.filinghistory.consumer.exception.NonRetryableException;
import uk.gov.companieshouse.filinghistory.consumer.service.TransactionKindResult;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.TransformerTestingUtils;

@ExtendWith(MockitoExtension.class)
class InternalFilingHistoryApiMapperTest {

    private static final String DELTA_AT = "20140815230459600643";
    private static final String ENTITY_ID = "3056742847";
    private static final String ENCODED_ID = "MzA1Njc0Mjg0N3NqYXNqamQ";
    private static final String BARCODE = "XAITVXAX";
    private static final String ORIGINAL_DESCRIPTION = "Appointment terminated, director john tester";
    private static final String DOCUMENT_ID = "000%s4682".formatted(BARCODE);
    private static final String COMPANY_NUMBER = "12345678";
    private static final String UPDATED_BY = "updatedBy";
    private static final ObjectMapper MAPPER = TransformerTestingUtils.getMapper();

    @InjectMocks
    private InternalFilingHistoryApiMapper mapper;
    @Mock
    private OriginalValuesMapper originalValuesMapper;
    @Mock
    private ExternalDataMapper externalDataMapper;


    @Mock
    private InternalDataOriginalValues internalDataOriginalValues;
    @Mock
    private ExternalData externalData;


    @Test
    void shouldMapJsonNodeToRequestBody() {
        // given
        when(originalValuesMapper.map(any())).thenReturn(internalDataOriginalValues);
        when(externalDataMapper.mapExternalData(any(), any(), any(), any(), any())).thenReturn(externalData);

        JsonNode topLevelNode = buildJsonNode();
        JsonNode originalValuesNode = topLevelNode.get("original_values");

        InternalFilingHistoryApiMapperArguments arguments = new InternalFilingHistoryApiMapperArguments(
                topLevelNode,
                buildTransactionKindResult(),
                COMPANY_NUMBER,
                DELTA_AT,
                UPDATED_BY);
        InternalFilingHistoryApi expected = buildExpectedTM01RequestBody();

        // when
        InternalFilingHistoryApi actualRequestBody = mapper.mapInternalFilingHistoryApi(arguments);

        // then
        assertEquals(expected, actualRequestBody);
        verify(originalValuesMapper).map(originalValuesNode);
        verify(externalDataMapper).mapExternalData(topLevelNode, BARCODE, DOCUMENT_ID, ENCODED_ID, COMPANY_NUMBER);
    }

    @Test
    void shouldMapRequestBodyWithNullFields() {
        // given
        InternalFilingHistoryApi expected = new InternalFilingHistoryApi()
                .externalData(null)
                .internalData(new InternalData()
                        .transactionKind(TOP_LEVEL)
                        .deltaAt(null)
                        .originalValues(null)
                        .originalDescription(null)
                        .companyNumber(null)
                        .parentEntityId(null)
                        .entityId(null)
                        .documentId(null)
                        .updatedBy(null));

        InternalFilingHistoryApiMapperArguments arguments = new InternalFilingHistoryApiMapperArguments(
                null,
                buildTransactionKindResult(),
                null,
                null,
                null);

        // when
        InternalFilingHistoryApi actualRequestBody = mapper.mapInternalFilingHistoryApi(arguments);

        // then
        assertEquals(expected, actualRequestBody);
        verify(originalValuesMapper).map(null);
        verify(externalDataMapper).mapExternalData(null, null, null, ENCODED_ID, null);
    }

    @Test
    void shouldThrowNonRetryableExceptionWhenNullTransactionKindResult() {
        // given
        InternalFilingHistoryApiMapperArguments arguments = new InternalFilingHistoryApiMapperArguments(
                null,
                null,
                null,
                null,
                null);

        // when
        Executable actual = () -> mapper.mapInternalFilingHistoryApi(arguments);

        // then
        assertThrows(NonRetryableException.class, actual);
        verifyNoInteractions(originalValuesMapper);
        verifyNoInteractions(externalDataMapper);
    }

    private static JsonNode buildJsonNode() {
        ObjectNode topLevelNode = MAPPER.createObjectNode()
                .put("_barcode", InternalFilingHistoryApiMapperTest.BARCODE)
                .put("original_description", ORIGINAL_DESCRIPTION)
                .put("parent_entity_id", "")
                .put("_entity_id", ENTITY_ID)
                .put("_document_id", InternalFilingHistoryApiMapperTest.DOCUMENT_ID)
                .put("company_number", InternalFilingHistoryApiMapperTest.COMPANY_NUMBER);

        topLevelNode.putObject("original_values");

        return topLevelNode;
    }

    private static TransactionKindResult buildTransactionKindResult() {
        return new TransactionKindResult(ENCODED_ID, TOP_LEVEL);
    }

    private InternalFilingHistoryApi buildExpectedTM01RequestBody() {
        return new InternalFilingHistoryApi()
                .externalData(externalData)
                .internalData(new InternalData()
                        .transactionKind(TOP_LEVEL)
                        .deltaAt(DELTA_AT)
                        .originalValues(internalDataOriginalValues)
                        .originalDescription(ORIGINAL_DESCRIPTION)
                        .companyNumber(COMPANY_NUMBER)
                        .parentEntityId("")
                        .entityId(ENTITY_ID)
                        .documentId(DOCUMENT_ID)
                        .updatedBy(UPDATED_BY));
    }
}
