package uk.gov.companieshouse.filinghistory.consumer.mapper.pretransform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.TransformerTestingUtils;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.FormatDate;

@ExtendWith(MockitoExtension.class)
class AssociatedFilingNodeMapperTest {

    private static final ObjectMapper objectMapper = TransformerTestingUtils.getMapper();

    private AssociatedFilingNodeMapper associatedFilingNodeMapper;
    @Mock
    private FormatDate formatDate;

    @Mock
    private FilingHistory delta;

    @BeforeEach
    void setUp() {
        associatedFilingNodeMapper = new AssociatedFilingNodeMapper(objectMapper, formatDate);
    }

    @ParameterizedTest
    @CsvSource({
            "'', ''",
            "description, description",
            "'description<with\nextra chars', 'description\\with\\extra chars'"})
    void shouldMapAssociatedFilingObjectNode(final String deltaDescription, final String expectedDescription) {
        // given
        when(delta.getFormType()).thenReturn("form type");
        when(formatDate.format(any())).thenReturn("date");
        when(delta.getDescription()).thenReturn(deltaDescription);

        ObjectNode parentNode = objectMapper.createObjectNode();
        parentNode.putObject("data");

        ObjectNode expected = objectMapper.createObjectNode();
        expected.putObject("data")
                .putArray("associated_filings")
                .addObject()
                .put("type", "form type")
                .put("date", "date")
                .put("description", expectedDescription);

        // when
        ObjectNode actual = associatedFilingNodeMapper.mapChildObjectNode(delta, parentNode);

        // then
        assertEquals(expected, actual);
    }
}
