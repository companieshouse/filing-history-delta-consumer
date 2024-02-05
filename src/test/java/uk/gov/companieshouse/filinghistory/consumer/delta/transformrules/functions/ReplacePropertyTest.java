package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.TransformerTestingUtils;

class ReplacePropertyTest {

    private static final ObjectMapper MAPPER = TransformerTestingUtils.getMapper();
    private ReplaceProperty replaceProperty;
    private final LowerCase lowerCase = TransformerTestingUtils.getLowerCase();

    @BeforeEach
    void setUp() {
        replaceProperty = new ReplaceProperty(MAPPER, lowerCase);
    }

    @Test
    void shouldReplacePropertyWithListOfValues() {
        // given
        ObjectNode source = MAPPER.createObjectNode();
        ObjectNode actual = source.deepCopy();

        ObjectNode expected = MAPPER.createObjectNode();
        expected
                .putObject("data")
                .putArray("subcategory")
                .add("voluntary")
                .add("certificate");
        // when
        replaceProperty.transform(source, actual, "data.subcategory", List.of("voluntary", "certificate"),
                Map.of());

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldReplacePropertyWithString() {
        // given
        ObjectNode source = MAPPER.createObjectNode();
        ObjectNode actual = source.deepCopy();

        ObjectNode expected = MAPPER.createObjectNode();
        expected
                .putObject("data")
                .put("description", "liquidation-voluntary-removal-liquidator");

        // when
        replaceProperty.transform(source, actual, "data.description",
                List.of("liquidation-voluntary-removal-liquidator"), Map.of());

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldReplacePropertyWithSearchAndReplace() {
        // given
        ObjectNode source = MAPPER.createObjectNode();
        ObjectNode actual = source.deepCopy();

        ObjectNode expected = MAPPER.createObjectNode();
        expected
                .putObject("data")
                .put("description", "accounts-with-accounts-type-full-group");

        // when
        replaceProperty.transform(source, actual, "data.description",
                List.of("accounts-with-accounts-type-[% accountsType | lc %]-group"), Map.of("accountsType", "FULL"));

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldThrowExceptionWHenFunctionNotLowerCaseWithinSearchAndReplace() {
        // given
        ObjectNode source = MAPPER.createObjectNode();
        ObjectNode copy = source.deepCopy();

        // when
        Executable actual = () -> replaceProperty.transform(source, copy, "data.description",
                List.of("accounts-with-accounts-type-[% accountsType | address_case %]-group"), Map.of("accountsType", "FULL"));

        // then
        assertThrows(IllegalArgumentException.class, actual);
    }
}