package uk.gov.companieshouse.filinghistory.consumer.mapper.pretransform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.DescriptionValues;
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.FormatDate;

@ExtendWith(MockitoExtension.class)
class ResolutionNodeMapperTest {

    private static final String CASE_START_DATE = "01/01/2010";
    private static final String RES_TYPE = "res type";
    private static final String DESCRIPTION = "description";
    private static final String DATE = "02/02/2011";
    private static final String RESOLUTION_DATE = "03/03/2012";
    @InjectMocks
    private ResolutionNodeMapper resolutionNodeMapper;

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private FormatDate formatDate;

    @Mock
    private FilingHistory delta;

    @ParameterizedTest
    @CsvSource({
            "''",
            DESCRIPTION})
    void shouldMapResolutionObjectNode(final String description) {
        // given
        ObjectNode actualObjectNode = new ObjectMapper().createObjectNode();
        when(objectMapper.createObjectNode()).thenReturn(actualObjectNode);
        when(delta.getFormType()).thenReturn("form type");
        when(delta.getDescription()).thenReturn(description);
        when(formatDate.format(any())).thenReturn("date");

        DescriptionValues descriptionValues = new DescriptionValues()
                .caseStartDate(CASE_START_DATE)
                .resType(RES_TYPE)
                .description(DESCRIPTION)
                .date(DATE)
                .resolutionDate(RESOLUTION_DATE);

        when(delta.getDescriptionValues()).thenReturn(descriptionValues);

        ObjectNode expectedObjectNode = new ObjectMapper()
                .createObjectNode()
                .put("type", "form type")
                .put("date", "date")
                .put(DESCRIPTION, description);

        expectedObjectNode
                .putObject("description_values")
                .put("case_start_date", CASE_START_DATE)
                .put("res_type", RES_TYPE)
                .put("description", DESCRIPTION)
                .put("date", DATE)
                .put("resolution_date", RESOLUTION_DATE);

        ChildPair expectedChildPair = new ChildPair("resolutions", expectedObjectNode);

        // when
        ChildPair actualChildPair = resolutionNodeMapper.mapChildObjectNode(delta);

        // then
        assertEquals(expectedChildPair, actualChildPair);
    }
}