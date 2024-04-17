package uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.filinghistory.consumer.serdes.ArrayNodeDeserialiser;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.TransformerTestingUtils;

@ExtendWith(MockitoExtension.class)
class SubcategoryMapperTest {

    private static final ObjectMapper MAPPER = TransformerTestingUtils.getMapper();
    @InjectMocks
    private SubcategoryMapper mapper;
    @Mock
    private ArrayNodeDeserialiser<String> deserialiser;

    @Test
    void shouldMapSubcategoryWhenString() {
        // given
        ObjectNode node = MAPPER.createObjectNode()
                .put("subcategory", "termination");

        Object expected = "termination";

        // when
        Object actual = mapper.map(node);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldMapSubcategoryWhenArrayOfStrings() {
        // given
        ObjectNode node = MAPPER.createObjectNode();
        node
                .putArray("subcategory")
                .add("voluntary")
                .add("certificate");

        List<String> expected = List.of("voluntary", "certificate");

        when(deserialiser.deserialise(any())).thenReturn(expected);

        // when
        Object actual = mapper.map(node);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldMapSubcategoryWhenNull() {
        // given
        ObjectNode node = MAPPER.createObjectNode();

        // when
        Object actual = mapper.map(node);

        // then
        assertNull(actual);
    }

    @Test
    void shouldMapSubcategoryWhenNullDataNode() {
        // given

        // when
        Object actual = mapper.map(null);

        // then
        assertNull(actual);
    }
}