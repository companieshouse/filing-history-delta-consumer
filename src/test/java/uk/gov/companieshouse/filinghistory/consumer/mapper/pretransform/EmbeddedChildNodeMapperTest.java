package uk.gov.companieshouse.filinghistory.consumer.mapper.pretransform;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.ChildProperties;
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.TransformerTestingUtils;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.FormatDate;

@ExtendWith(MockitoExtension.class)
class EmbeddedChildNodeMapperTest {

    private static final ObjectMapper objectMapper = TransformerTestingUtils.getMapper();

    private EmbeddedChildNodeMapper embeddedChildNodeMapper;
    @Mock
    private FormatDate formatDate;

    @BeforeEach
    void setUp() {
        embeddedChildNodeMapper = new EmbeddedChildNodeMapper(objectMapper, formatDate);
    }

    @ParameterizedTest
    @CsvSource({
            "''",
            "description"})
    void shouldMapAssociatedFilingObjectNode(final String description) {
        // given
        FilingHistory delta = new FilingHistory()
                .child(List.of(new ChildProperties()
                        .formType("form type")
                        .receiveDate("date")
                        .description(description)));

        when(formatDate.format(any())).thenReturn("date");

        ObjectNode parentNode = objectMapper.createObjectNode();
        parentNode.putObject("data");

        ObjectNode expected = objectMapper.createObjectNode();
        expected.putObject("data")
                .putArray("associated_filings")
                .addObject()
                .put("type", "form type")
                .put("date", "date")
                .put("description", description);

        // when
        ObjectNode actual = embeddedChildNodeMapper.mapChildObjectNode(delta, parentNode);

        // then
        assertEquals(expected, actual);
    }
}
