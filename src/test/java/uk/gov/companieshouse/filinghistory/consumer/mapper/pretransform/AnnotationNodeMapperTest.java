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
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.FormatDate;

@ExtendWith(MockitoExtension.class)
class AnnotationNodeMapperTest {

    @InjectMocks
    private AnnotationNodeMapper annotationNodeMapper;

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private FormatDate formatDate;

    @Mock
    private FilingHistory delta;

    @ParameterizedTest
    @CsvSource({
            "''",
            "description"})
    void shouldMapAnnotationObjectNode(final String description) {
        // given
        ObjectNode actualObjectNode = new ObjectMapper().createObjectNode();
        when(objectMapper.createObjectNode()).thenReturn(actualObjectNode);
        when(delta.getFormType()).thenReturn("form type");
        when(delta.getDescription()).thenReturn(description);
        when(formatDate.format(any())).thenReturn("date");

        ObjectNode expectedObjectNode = new ObjectMapper()
                .createObjectNode()
                .put("type", "form type")
                .put("date", "date")
                .put("annotation", description);

        ChildPair expectedChildPair = new ChildPair("annotations", expectedObjectNode);

        // when
        ChildPair actualChildPair = annotationNodeMapper.mapChildObjectNode(delta);

        // then
        assertEquals(expectedChildPair, actualChildPair);
    }
}
