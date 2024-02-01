package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.TransformerTestingUtils;

@ExtendWith(MockitoExtension.class)
class ProcessCapitalTest {

    private static final Pattern EXTRACT = Pattern.compile(
            "(?i:\\bSTATEMENT OF CAPITAL[; ](?<capitalCurrency>\\w+) (?<capitalFigure>\\d+\\.\\d+|\\.\\d+|\\d+))");
    private static final Pattern ALT_EXTRACT = Pattern.compile(
            "(?i:(?<capitalDate>\\d+\\D\\d+\\D\\d+) (?:(?<capitalDesc>STATEMENT OF CAPITAL)|(?<capitalAltDesc>TREASURY CAPITAL)) (?<capitalCurrency>\\w+) (?<capitalFigure>\\d+\\.\\d+|\\.\\d+|\\d+))");
    public static final String DATA_DESCRIPTION_FIELD_PATH = "data.description";
    private static final String ALT_DESCRIPTION = "capital-cancellation-treasury-shares-with-date-treasury-capital-figure";
    private static final ObjectMapper objectMapper = TransformerTestingUtils.getMapper();
    public static final String SOURCE_DESCRIPTION = """
            Second filed SH02 - 03/02/16\s
            Statement of Capital gbp 1000 03/02/16\s
            Statement of Capital eur 2000 03/02/16\s
            Statement of Capital usd 3000""";
    public static final String ALT_SOURCE_DESCRIPTION = """
            13/05/14 Statement of Capital USD 1000\s
            13/05/14 Treasury Capital GBP 50000\s
            13/05/14 Statement of Capital HKD 3333\s
            13/05/14 Statement of Capital JPY 221""";

    private ProcessCapital processCapital;
    @Mock
    private CapitalCaptor capitalCaptor;
    @Mock
    private CapitalCaptures capitalCaptures;
    @Mock
    private JsonNode capture;

    @BeforeEach
    void setup() {
        processCapital = new ProcessCapital(objectMapper, capitalCaptor);
    }

    @Test
    void shouldTransformCaptureGroupsIntoCapitalNode() {
        // given
        ArrayNode captures = objectMapper.createArrayNode()
                .add(capture);

        when(capitalCaptor.captureCapital(any(), any(), any())).thenReturn(capitalCaptures);
        when(capitalCaptures.captures()).thenReturn(captures);
        when(capitalCaptures.altCaptures()).thenReturn(objectMapper.createArrayNode());

        JsonNode source = buildSourceNode();

        JsonNode expected = buildExpectedNode(captures);

        // when
        ObjectNode actual = source.deepCopy();
        processCapital.transform(source, actual, DATA_DESCRIPTION_FIELD_PATH, EXTRACT, null);

        // then
        assertEquals(expected, actual);
        verify(capitalCaptor).captureCapital(EXTRACT, null, SOURCE_DESCRIPTION);
    }

    @Test
    void shouldTransformBothCaptureAndAltCaptureGroupsIntoCapitalAndAltCapitalNodes() {
        // given
        ArrayNode altCaptures = objectMapper.createArrayNode()
                .add(capture);
        ArrayNode captures = objectMapper.createArrayNode()
                .add(capture);

        when(capitalCaptor.captureCapital(any(), any(), any())).thenReturn(capitalCaptures);
        when(capitalCaptures.captures()).thenReturn(captures);
        when(capitalCaptures.altCaptures()).thenReturn(altCaptures);

        JsonNode source = buildSourceAltNode();

        JsonNode expected = buildExpectedAltNode(altCaptures, captures);

        // when
        ObjectNode actual = source.deepCopy();
        processCapital.transform(source, actual, DATA_DESCRIPTION_FIELD_PATH, ALT_EXTRACT, ALT_DESCRIPTION);

        // then
        assertEquals(expected, actual);
        verify(capitalCaptor).captureCapital(ALT_EXTRACT, ALT_DESCRIPTION, ALT_SOURCE_DESCRIPTION);
    }

    private static JsonNode buildSourceNode() {
        final ObjectNode topLevelNode = objectMapper.createObjectNode()
                .put("_entity_id", "3063732185")
                .put("_barcode", "XAITVXAX");

        topLevelNode
                .putObject("data")
                .put("type", "RP04SH02")
                .put("description",
                        SOURCE_DESCRIPTION);

        return topLevelNode;
    }

    private static JsonNode buildSourceAltNode() {
        final ObjectNode topLevelNode = objectMapper.createObjectNode()
                .put("_entity_id", "3081501305")
                .put("_barcode", "13CMU31L");

        topLevelNode
                .putObject("data")
                .put("type", "SH05")
                .put("description",
                        ALT_SOURCE_DESCRIPTION);

        return topLevelNode;
    }

    private JsonNode buildExpectedNode(ArrayNode captures) {
        final ObjectNode topLevelNode = objectMapper.createObjectNode()
                .put("_entity_id", "3063732185")
                .put("_barcode", "XAITVXAX");

        topLevelNode
                .putObject("data")
                .put("type", "RP04SH02")
                .put("description", // this gets set to its enum value when applying the set rules
                        SOURCE_DESCRIPTION)
                .putObject("description_values")
                .putArray("capital")
                .addAll(captures);

        return topLevelNode;
    }

    private static JsonNode buildExpectedAltNode(ArrayNode altCaptures, ArrayNode captures) {
        final ObjectNode topLevelNode = objectMapper.createObjectNode()
                .put("_entity_id", "3081501305")
                .put("_barcode", "13CMU31L");

        topLevelNode
                .putObject("data")
                .put("type", "SH05")
                .put("description", // this gets set to its enum value when applying the set rules
                        ALT_SOURCE_DESCRIPTION);

        ObjectNode descriptionValues = ((ObjectNode) topLevelNode.at("/data")).putObject("description_values");

        descriptionValues
                .putArray("alt_capital")
                .addAll(altCaptures);
        descriptionValues
                .putArray("capital")
                .addAll(captures);

        return topLevelNode;
    }
}