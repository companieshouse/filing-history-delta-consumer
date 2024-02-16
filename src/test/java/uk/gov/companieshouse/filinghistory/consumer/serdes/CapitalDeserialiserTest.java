package uk.gov.companieshouse.filinghistory.consumer.serdes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.filinghistory.AltCapitalDescriptionValue;
import uk.gov.companieshouse.api.filinghistory.CapitalDescriptionValue;
import uk.gov.companieshouse.filinghistory.consumer.exception.NonRetryableException;

@ExtendWith(MockitoExtension.class)
class CapitalDeserialiserTest {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .registerModule(new JavaTimeModule());

    private CapitalDeserialiser deserialiser;

    @BeforeEach
    void setUp() {
        deserialiser = new CapitalDeserialiser(objectMapper);
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
        List<CapitalDescriptionValue> actual = deserialiser.deserialiseCapitalArray(capital);

        // then
        assertEquals(expected, actual);
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
        List<AltCapitalDescriptionValue> actual = deserialiser.deserialiseAltCapitalArray(altCapital);

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
        Executable executable = () -> deserialiser.deserialiseCapitalArray(capital);

        // then
        NonRetryableException actual = assertThrows(NonRetryableException.class, executable);
        assertEquals("Unable to deserialise capital array: [%s]".formatted(capital.toPrettyString()),
                actual.getMessage());
    }

    @Test
    void shouldThrowNonRetryableExceptionWhenIOExceptionDuringDeserialiseAltCapital() {
        // given
        ArrayNode altCapital = objectMapper.createArrayNode();
        altCapital.addObject()
                .put("unknown_field", "unknown");

        // when
        Executable executable = () -> deserialiser.deserialiseAltCapitalArray(altCapital);

        // then
        NonRetryableException actual = assertThrows(NonRetryableException.class, executable);
        assertEquals("Unable to deserialise alt capital array: [%s]".formatted(altCapital.toPrettyString()),
                actual.getMessage());
    }
}