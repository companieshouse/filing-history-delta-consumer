package uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.Annotation;
import uk.gov.companieshouse.api.filinghistory.DescriptionValues;
import uk.gov.companieshouse.api.filinghistory.ExternalData;
import uk.gov.companieshouse.api.filinghistory.ExternalData.CategoryEnum;
import uk.gov.companieshouse.api.filinghistory.Links;
import uk.gov.companieshouse.filinghistory.consumer.serdes.ArrayNodeDeserialiser;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.TransformerTestingUtils;

@ExtendWith(MockitoExtension.class)
class ExternalDataMapperTest {

    private static final String ENCODED_ID = "MzA1Njc0Mjg0N3NqYXNqamQ";
    private static final String BARCODE = "XAITVXAX";
    private static final String DESCRIPTION = "termination-director-company-with-name-termination-date";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String DATE = "20110905053919";
    private static final String TYPE = "TM01";
    private static final String CATEGORY = "officers";
    private static final String SUBCATEGORY = "termination";
    private static final ObjectMapper MAPPER = TransformerTestingUtils.getMapper();

    @InjectMocks
    private ExternalDataMapper externalDataMapper;
    @Mock
    private SubcategoryMapper subcategoryMapper;
    @Mock
    private DescriptionValuesMapper descriptionValuesMapper;
    @Mock
    private PaperFiledMapper paperFiledMapper;
    @Mock
    private LinksMapper linksMapper;
    @Mock
    private ArrayNodeDeserialiser<Annotation> annotationsDeserialiser;

    @Mock
    private Object subcategory;
    @Mock
    private DescriptionValues descriptionValues;
    @Mock
    private Links links;
    @Mock
    private ArrayNode annotations;
    @Mock
    private Annotation annotation;

    @Test
    void shouldMapExternalData() {
        // given
        when(subcategoryMapper.map(any())).thenReturn(subcategory);
        when(descriptionValuesMapper.map(any())).thenReturn(descriptionValues);
        when(paperFiledMapper.isPaperFiled(any())).thenReturn(true);
        when(linksMapper.map(any(), any())).thenReturn(links);
        when(annotationsDeserialiser.deserialise(any())).thenReturn(List.of(annotation));

        JsonNode topLevelNode = buildJsonNode();
        JsonNode dataNode = topLevelNode.get("data");
        JsonNode descriptionValuesNode = dataNode.get("description_values");

        ExternalData expected = buildExpectedData();

        // when
        ExternalData actual = externalDataMapper.mapExternalData(topLevelNode, BARCODE, ENCODED_ID,
                COMPANY_NUMBER);

        // then
        assertEquals(expected, actual);
        verify(subcategoryMapper).map(dataNode);
        verify(descriptionValuesMapper).map(descriptionValuesNode);
        verify(paperFiledMapper).isPaperFiled(BARCODE);
        verify(linksMapper).map(COMPANY_NUMBER, ENCODED_ID);
        verify(annotationsDeserialiser).deserialise(annotations);
    }

    @Test
    void shouldMapExternalDataWithNullFields() {
        // given
        ExternalData expected = new ExternalData();
        when(paperFiledMapper.isPaperFiled(any())).thenReturn(false);

        // when
        ExternalData actual = externalDataMapper.mapExternalData(null, null, null, null);

        // then
        assertEquals(expected, actual);
        verify(subcategoryMapper).map(null);
        verify(descriptionValuesMapper).map(null);
        verify(paperFiledMapper).isPaperFiled(null);
        verify(linksMapper).map(null, null);
        verifyNoInteractions(annotationsDeserialiser);
    }

    private JsonNode buildJsonNode() {
        ObjectNode topLevelNode = MAPPER.createObjectNode();
        ObjectNode dataNode = topLevelNode.putObject("data");

        dataNode.put("type", TYPE)
                .put("date", DATE)
                .put("category", CATEGORY)
                .put("subcategory", SUBCATEGORY)
                .put("description", DESCRIPTION)
                .put("action_date", DATE);

        dataNode.putObject("description_values");
        dataNode.putIfAbsent("annotations", annotations);

        return topLevelNode;
    }

    private ExternalData buildExpectedData() {
        return new ExternalData()
                .transactionId(ENCODED_ID)
                .barcode(BARCODE)
                .type(TYPE)
                .date(DATE)
                .category(CategoryEnum.OFFICERS)
                .subcategory(subcategory)
                .description(DESCRIPTION)
                .descriptionValues(descriptionValues)
                .actionDate(DATE)
                .links(links)
                .paperFiled(true)
                .annotations(List.of(annotation));
    }
}