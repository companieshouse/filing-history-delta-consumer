package uk.gov.companieshouse.filinghistory.consumer.serdes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import uk.gov.companieshouse.api.filinghistory.AltCapitalDescriptionValue;
import uk.gov.companieshouse.api.filinghistory.Annotation;
import uk.gov.companieshouse.api.filinghistory.AssociatedFiling;
import uk.gov.companieshouse.api.filinghistory.CapitalDescriptionValue;
import uk.gov.companieshouse.api.filinghistory.DescriptionValues;
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

    @Test
    void shouldDeserialiseAnnotationsArrayNode() {
        // given
        ArrayNode annotations = objectMapper.createArrayNode();
        annotations.addObject()
                .put("annotation", "reason for the annotation")
                .put("category", "annotation")
                .put("date", "2011-11-26T11:27:55Z")
                .put("description", "annotation")
                .put("type", "ANNOTATION")
                .putObject("description_values")
                .put("description", "reason for the annotation");

        List<Annotation> expected = List.of(
                new Annotation()
                        .annotation("reason for the annotation")
                        .category("annotation")
                        .date("2011-11-26T11:27:55Z")
                        .description("annotation")
                        .type("ANNOTATION")
                        .descriptionValues(new DescriptionValues()
                                .description("reason for the annotation")));

        // when
        List<Annotation> actual = serdesConfig.annotationArrayNodeDeserialiser(objectMapper)
                .deserialise(annotations);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldThrowNonRetryableExceptionWhenIOExceptionDuringDeserialiseAnnotations() {
        // given
        ArrayNode annotations = objectMapper.createArrayNode();
        annotations.addObject()
                .put("unknown_field", "unknown");

        // when
        Executable executable = () -> serdesConfig.annotationArrayNodeDeserialiser(objectMapper)
                .deserialise(annotations);

        // then
        NonRetryableException actual = assertThrows(NonRetryableException.class, executable);
        assertEquals("Unable to deserialise array node: [%s]".formatted(annotations.toPrettyString()),
                actual.getMessage());
    }

    @Test
    void shouldDeserialiseAssociatedFilingsArrayNode() {
        // given
        ArrayNode associatedFilings = objectMapper.createArrayNode();
        associatedFilings.addObject()
                .put("category", "incorporation")
                .put("date", "2011-11-26T11:27:55Z")
                .put("description", "associated filing")
                .put("type", "MODEL ARTICLES")
                .putObject("description_values")
                .put("description", "reason for the af");

        List<AssociatedFiling> expected = List.of(
                new AssociatedFiling()
                        .category("incorporation")
                        .date("2011-11-26T11:27:55Z")
                        .description("associated filing")
                        .type("MODEL ARTICLES")
                        .descriptionValues(new DescriptionValues()
                                .description("reason for the af")));

        // when
        List<AssociatedFiling> actual = serdesConfig.associatedFilingArrayNodeDeserialiser(objectMapper)
                .deserialise(associatedFilings);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldThrowNonRetryableExceptionWhenIOExceptionDuringDeserialiseAssociatedFilings() {
        // given
        ArrayNode associatedFilings = objectMapper.createArrayNode();
        associatedFilings.addObject()
                .put("unknown_field", "unknown");

        // when
        Executable executable = () -> serdesConfig.associatedFilingArrayNodeDeserialiser(objectMapper)
                .deserialise(associatedFilings);

        // then
        NonRetryableException actual = assertThrows(NonRetryableException.class, executable);
        assertEquals("Unable to deserialise array node: [%s]".formatted(associatedFilings.toPrettyString()),
                actual.getMessage());
    }
}
