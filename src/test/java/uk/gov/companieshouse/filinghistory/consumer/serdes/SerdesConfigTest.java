package uk.gov.companieshouse.filinghistory.consumer.serdes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import uk.gov.companieshouse.api.filinghistory.AltCapitalDescriptionValue;
import uk.gov.companieshouse.api.filinghistory.CapitalDescriptionValue;
import uk.gov.companieshouse.filinghistory.consumer.exception.NonRetryableException;

class SerdesConfigTest {

    private final SerdesConfig serdesConfig = new SerdesConfig();
    private final ObjectMapper objectMapper = serdesConfig.objectMapper();

    @Test
    void shouldDeserialiseStringArrayNode() {
        // given
        ArrayNode arrayNode = objectMapper.createArrayNode()
                .add("voluntary")
                .add("certificate");

        List<String> expected = List.of("voluntary", "certificate");

        // when
        List<String> actual = serdesConfig.stringArrayNodeDeserialiser(objectMapper).deserialise(arrayNode);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldThrowNonRetryableExceptionWhenIOExceptionDuringDeserialise() {
        // given
        ArrayNode arrayNode = objectMapper.createArrayNode();
        arrayNode.addObject()
                .put("category", "invalid")
                .put("subcategory", "wrong");

        // when
        Executable executable = () -> serdesConfig.stringArrayNodeDeserialiser(objectMapper).deserialise(arrayNode);

        // then
        NonRetryableException actual = assertThrows(NonRetryableException.class, executable);
        assertEquals("Unable to deserialise array node: [%s]".formatted(arrayNode.toPrettyString()),
                actual.getMessage());
    }

    @Test
    void shouldDeserialiseAltCapitalArrayNode() {
        // given
        ArrayNode altCapital = objectMapper.createArrayNode();
        altCapital.addObject()
                .put("currency", "GBP")
                .put("figure", "1,000");

        List<AltCapitalDescriptionValue> expected = List.of(
                new AltCapitalDescriptionValue()
                        .currency("GBP")
                        .figure("1,000"));

        // when
        List<AltCapitalDescriptionValue> actual = serdesConfig.altCapitalArrayNodeDeserialiser(objectMapper)
                .deserialise(altCapital);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldThrowNonRetryableExceptionWhenIOExceptionDuringDeserialiseAltCapital() {
        // given
        ArrayNode altCapital = objectMapper.createArrayNode();
        altCapital.addObject()
                .put("unknown_field", "unknown");

        // when
        Executable executable = () -> serdesConfig.altCapitalArrayNodeDeserialiser(objectMapper)
                .deserialise(altCapital);

        // then
        NonRetryableException actual = assertThrows(NonRetryableException.class, executable);
        assertEquals("Unable to deserialise array node: [%s]".formatted(altCapital.toPrettyString()),
                actual.getMessage());
    }

    @Test
    void shouldDeserialiseCapitalArrayNode() {
        // given
        ArrayNode capital = objectMapper.createArrayNode();
        capital.addObject()
                .put("currency", "GBP")
                .put("figure", "1,000");

        List<CapitalDescriptionValue> expected = List.of(
                new CapitalDescriptionValue()
                        .currency("GBP")
                        .figure("1,000"));

        // when
        List<CapitalDescriptionValue> actual = serdesConfig.capitalArrayNodeDeserialiser(objectMapper)
                .deserialise(capital);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldThrowNonRetryableExceptionWhenIOExceptionDuringDeserialiseCapital() {
        // given
        ArrayNode capital = objectMapper.createArrayNode();
        capital.addObject()
                .put("unknown_field", "unknown");

        // when
        Executable executable = () -> serdesConfig.capitalArrayNodeDeserialiser(objectMapper)
                .deserialise(capital);

        // then
        NonRetryableException actual = assertThrows(NonRetryableException.class, executable);
        assertEquals("Unable to deserialise array node: [%s]".formatted(capital.toPrettyString()),
                actual.getMessage());
    }
}
