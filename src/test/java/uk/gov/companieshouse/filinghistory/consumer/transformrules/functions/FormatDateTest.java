package uk.gov.companieshouse.filinghistory.consumer.transformrules.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.companieshouse.filinghistory.consumer.exception.NonRetryableException;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.TransformerTestingUtils;

class FormatDateTest {

    private static final ObjectMapper MAPPER = TransformerTestingUtils.getMapper();

    private FormatDate formatDate;

    @BeforeEach
    void beforeEach() {
        formatDate = new FormatDate(MAPPER);
    }

    @Test
    void shouldTransformCaptureGroupValueToDateFormatAndSetOnOutputNode() {
        // given

        ObjectNode source = MAPPER.createObjectNode();
        ObjectNode actual = source.deepCopy();

        ObjectNode expected = MAPPER.createObjectNode();
        expected
                .putObject("data")
                .put("action_date", "2012-06-04T00:00:00Z");
        // when
        formatDate.transform(source, actual, "data.action_date", List.of("appointmentDate"),
                Map.of("appointmentDate", "04/06/2012"));

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldTransformSourceNodeValueToDateFormatAndSetOnOutputNode() {
        // given

        ObjectNode source = MAPPER.createObjectNode();
        source.putObject("original_values")
                .put("change_date", "1/01/2010");
        ObjectNode actual = source.deepCopy();

        ObjectNode expected = MAPPER.createObjectNode();
        expected.putObject("original_values")
                .put("change_date", "1/01/2010");
        expected
                .putObject("data")
                .put("action_date", "2010-01-01T00:00:00Z");
        // when
        formatDate.transform(source, actual, "data.action_date", List.of("original_values.change_date"),
                Map.of());

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldNotTransformSourceNodeValueIfAlreadyInCorrectFormatAndSetOnOutputNode() {
        // given

        ObjectNode source = MAPPER.createObjectNode();
        source.putObject("original_values")
                .put("change_date", "2016-07-02T11:17:16Z");
        ObjectNode actual = source.deepCopy();

        ObjectNode expected = MAPPER.createObjectNode();
        expected.putObject("original_values")
                .put("change_date", "2016-07-02T11:17:16Z");
        expected
                .putObject("data")
                .put("action_date", "2016-07-02T11:17:16Z");
        // when
        formatDate.transform(source, actual, "data.action_date", List.of("original_values.change_date"),
                Map.of());

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldNotTransformSourceNodeValueIfNull() {
        // given

        ObjectNode source = MAPPER.createObjectNode();
        source.putObject("original_values")
                .put("change_date", (String) null);
        ObjectNode actual = source.deepCopy();

        ObjectNode expected = MAPPER.createObjectNode();
        expected.putObject("original_values")
                .put("change_date", (String) null);
        expected
                .putObject("data")
                .put("action_date", (String) null);
        // when
        formatDate.transform(source, actual, "data.action_date", List.of("original_values.change_date"),
                Map.of());

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldThrowNonRetryableExceptionWhenSourceNodeValueCannotBeParsed() {
        // given
        ObjectNode source = MAPPER.createObjectNode();
        source.putObject("original_values")
                .put("change_date", "non date text");
        ObjectNode actual = source.deepCopy();

        // when
        Executable executable = () -> formatDate.transform(source, actual, "data.action_date",
                List.of("original_values.change_date"), Map.of());

        // then
        assertThrows(NonRetryableException.class, executable);
    }

    @ParameterizedTest(name = "Map [{0}] to [{1}]")
    @MethodSource("bsonDateFormatting")
    @DisplayName("Format text as a bson date")
    void testTransformSpecificMethodWithinTitleCaseTransformer(String input, String expected) {
        // given

        // when
        String output = formatDate.format(input);

        // then
        assertEquals(expected, output);
    }

    private static Stream<Arguments> bsonDateFormatting() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("", ""),
                Arguments.of("20201014221720", "2020-10-14T22:17:20Z"),
                Arguments.of("20211231", "2021-12-31T00:00:00Z"),
                Arguments.of("1/01/98", "1998-01-01T00:00:00Z"),
                Arguments.of("4/9/23", "2023-09-04T00:00:00Z"),
                Arguments.of("04/9/23", "2023-09-04T00:00:00Z"),
                Arguments.of("4/09/23", "2023-09-04T00:00:00Z"),
                Arguments.of("04/09/23", "2023-09-04T00:00:00Z"),
                Arguments.of("4/9/2023", "2023-09-04T00:00:00Z"),
                Arguments.of("04/9/2023", "2023-09-04T00:00:00Z"),
                Arguments.of("4/09/2023", "2023-09-04T00:00:00Z"),
                Arguments.of("31/12/2021", "2021-12-31T00:00:00Z")
        );
    }

}
