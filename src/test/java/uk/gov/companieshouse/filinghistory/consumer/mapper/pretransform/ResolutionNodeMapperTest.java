package uk.gov.companieshouse.filinghistory.consumer.mapper.pretransform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.DescriptionValues;
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.TransformerTestingUtils;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.FormatDate;

@ExtendWith(MockitoExtension.class)
class ResolutionNodeMapperTest {

    private static final ObjectMapper objectMapper = TransformerTestingUtils.getMapper();
    private static final String CASE_START_DATE = "01/01/2010";
    private static final String RES_TYPE = "res type";
    private static final String DESCRIPTION = "description";
    private static final String DATE = "02/02/2011";
    private static final String RESOLUTION_DATE = "03/03/2012";

    private ResolutionNodeMapper resolutionNodeMapper;
    @Mock
    private FormatDate formatDate;

    @Mock
    private FilingHistory delta;

    @BeforeEach
    void setUp() {
        resolutionNodeMapper = new ResolutionNodeMapper(objectMapper, formatDate);
    }

    @ParameterizedTest
    @CsvSource({
            "''",
            DESCRIPTION})
    void shouldMapResolutionObjectNode(final String description) {
        // given
        when(delta.getFormType()).thenReturn("form type");
        when(delta.getDescription()).thenReturn(description);

        DescriptionValues descriptionValues = new DescriptionValues()
                .caseStartDate(CASE_START_DATE)
                .resType(RES_TYPE)
                .description(DESCRIPTION)
                .date(DATE)
                .resolutionDate(RESOLUTION_DATE);

        when(delta.getDescriptionValues()).thenReturn(descriptionValues);

        ObjectNode parentNode = objectMapper.createObjectNode();
        parentNode.putObject("data");

        ObjectNode expected = objectMapper.createObjectNode();
        ObjectNode dataNode = expected.putObject("data");
        dataNode
                .put("type", "RESOLUTIONS")
                .put("description", "RESOLUTIONS");

        ObjectNode childNode = dataNode
                .putArray("resolutions")
                .addObject();
        childNode
                .put("type", "form type")
                .put(DESCRIPTION, description);

        childNode
                .putObject("description_values")
                .put("case_start_date", CASE_START_DATE)
                .put("res_type", RES_TYPE)
                .put("description", DESCRIPTION)
                .put("date", DATE)
                .put("resolution_date", RESOLUTION_DATE);

        // when
        ObjectNode actual = resolutionNodeMapper.mapChildObjectNode(delta, parentNode);

        // then
        assertEquals(expected, actual);
        verifyNoInteractions(formatDate);
    }

    @Test
    void shouldMapResolutionObjectNodeWithNoDescriptionValues() {
        // given
        when(delta.getFormType()).thenReturn("RES15");
        when(delta.getDescription()).thenReturn("description");

        ObjectNode parentNode = objectMapper.createObjectNode();
        parentNode.putObject("data");

        ObjectNode expected = objectMapper.createObjectNode();
        ObjectNode dataNode = expected.putObject("data");
        dataNode
                .put("type", "RESOLUTIONS")
                .put("description", "RESOLUTIONS");

        ObjectNode childNode = dataNode
                .putArray("resolutions")
                .addObject();
        childNode
                .put("type", "RES15")
                .put(DESCRIPTION, "description");

        // when
        ObjectNode actual = resolutionNodeMapper.mapChildObjectNode(delta, parentNode);

        // then
        assertEquals(expected, actual);
        verifyNoInteractions(formatDate);
    }
}