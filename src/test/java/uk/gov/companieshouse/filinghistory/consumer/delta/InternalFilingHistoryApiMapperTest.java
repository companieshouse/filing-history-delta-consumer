package uk.gov.companieshouse.filinghistory.consumer.delta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum.TOP_LEVEL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataAnnotations;
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
    private static final String ORIGINAL_DESCRIPTION = "Termination person judicial factor overseas company";
    private static final String DOCUMENT_ID = "000XAITVXAX4682";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String DATE = "20110905053919";
    private static final String TYPE = "TM01";
    private static final String CATEGORY = "officers";

    @InjectMocks
    private InternalFilingHistoryApiMapper mapper;

    @Mock
    private AnnotationsMapper annotationsMapper;
    @Mock
    private DescriptionValuesMapper descriptionValuesMapper;
    @Mock
    private LinksMapper linksMapper;
    @Mock
    private OriginalValuesMapper originalValuesMapper;

    @Mock
    private FilingHistoryItemDataAnnotations filingHistoryItemDataAnnotations;
    @Mock
    private FilingHistoryItemDataDescriptionValues filingHistoryItemDataDescriptionValues;
    @Mock
    private FilingHistoryItemDataLinks filingHistoryItemDataLinks;
    @Mock
    private InternalDataOriginalValues internalDataOriginalValues;


    @Test
    void shouldMapTransformedJsonNodeToInternalFilingHistoryApiObject() {
        // given
        when(annotationsMapper.map(any())).thenReturn(List.of(filingHistoryItemDataAnnotations));
        when(descriptionValuesMapper.map(any())).thenReturn(filingHistoryItemDataDescriptionValues);
        when(linksMapper.map(any())).thenReturn(filingHistoryItemDataLinks);
        when(originalValuesMapper.map(any())).thenReturn(internalDataOriginalValues);

        final InternalFilingHistoryApi expectedRequestBody = new InternalFilingHistoryApi()
                .externalData(new ExternalData()
                        .transactionId(ENCODED_ID)
                        .barcode(BARCODE)
                        .type(TYPE)
                        .date(DATE)
                        .category(ExternalData.CategoryEnum.OFFICERS)
                        .annotations(List.of(filingHistoryItemDataAnnotations))
                        .subcategory(ExternalData.SubcategoryEnum.OFFICERS)
                        .description(DESCRIPTION)
                        .descriptionValues(filingHistoryItemDataDescriptionValues)
                        .actionDate(DATE)
                        .links(filingHistoryItemDataLinks))
                .internalData(new InternalData()
                        .transactionKind(TOP_LEVEL)
                        .deltaAt(DELTA_AT)
                        .originalValues(internalDataOriginalValues)
                        .originalDescription(ORIGINAL_DESCRIPTION)
                        .companyNumber(COMPANY_NUMBER)
                        .parentEntityId("")
                        .entityId(ENTITY_ID)
                        .documentId(DOCUMENT_ID));

        // when
        final InternalFilingHistoryApi actualRequestBody = mapper.mapJsonNodeToInternalFilingHistoryApi(buildJsonNode(), buildTransactionKindResult(), DELTA_AT);

        // then
        assertEquals(expectedRequestBody, actualRequestBody);
    }

    private static JsonNode buildJsonNode() {
        final ObjectMapper objectMapper =
                new ObjectMapper()
                        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                        .registerModule(new JavaTimeModule());

        ObjectNode topLevelNode = objectMapper.createObjectNode()
                .put("_barcode", BARCODE)
                .put("original_description", ORIGINAL_DESCRIPTION)
                .put("parent_entity_id", "")
                .put("_entity_id", ENTITY_ID)
                .put("_document_id", DOCUMENT_ID);

        topLevelNode.putObject("data")
                .put("type", TYPE)
                .put("date", DATE)
                .put("category", CATEGORY)
                .put("subcategory", CATEGORY)
                .put("description", DESCRIPTION)
                .put("action_date", DATE)
                .putObject("description_values")
                    .put("company_number", COMPANY_NUMBER);

        return topLevelNode;
    }

    private static TransactionKindResult buildTransactionKindResult() {
        return new TransactionKindResult(ENCODED_ID, TOP_LEVEL);
    }
}
