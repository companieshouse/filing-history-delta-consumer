package uk.gov.companieshouse.filinghistory.consumer.mapper;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.ChildProperties;
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.FormatDate;

@ExtendWith(MockitoExtension.class)
class EmbeddedChildNodeMapperTest {

    @InjectMocks
    private EmbeddedChildNodeMapper embeddedChildNodeMapper;

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private FormatDate formatDate;

    @ParameterizedTest
    @CsvSource({
            "''",
            "description"})
    void shouldMapAssociatedFilingObjectNode(final String description) {
        // given
        ObjectNode actualObjectNode = new ObjectMapper().createObjectNode();

        FilingHistory delta = new FilingHistory()
                .child(List.of(new ChildProperties()
                        .formType("form type")
                        .receiveDate("date")
                        .description(description)));

        when(objectMapper.createObjectNode()).thenReturn(actualObjectNode);
        when(formatDate.format(any())).thenReturn("date");

        ObjectNode expectedObjectNode = new ObjectMapper()
                .createObjectNode()
                .put("type", "form type")
                .put("date", "date")
                .put("description", description);

        ChildPair expectedChildPair = new ChildPair("associated_filings", expectedObjectNode);

        // when
        ChildPair actualChildPair = embeddedChildNodeMapper.mapChildObjectNode(delta);

        // then
        assertEquals(expectedChildPair, actualChildPair);
    }
}
