package uk.gov.companieshouse.filinghistory.consumer.transformrules.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.TransformerTestingUtils;

class AnnotationTransformerTest {

    private static final ObjectMapper MAPPER = TransformerTestingUtils.getMapper();

    private AnnotationTransformer annotationTransformer;

    @BeforeEach
    void beforeEach() {
        annotationTransformer = new AnnotationTransformer();
    }

    @ParameterizedTest
    @CsvSource({
            "category",
            "description"
    })
    void shouldTransformFieldToHaveValueOfAnnotation(final String field) {
        // given
        ObjectNode source = MAPPER.createObjectNode();
        ObjectNode actual = source.deepCopy();

        ObjectNode expected = MAPPER.createObjectNode();
        expected.put(field, "annotation");

        // when
        annotationTransformer.transform(source, actual, field, List.of("annotation"), Collections.emptyMap());

        // then
        assertEquals(expected, actual);
    }
}