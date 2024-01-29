package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessCapitalTest {

    private static final Pattern EXTRACT = Pattern.compile(
            "(?i:\\bSTATEMENT OF CAPITAL[; ](?<capitalCurrency>\\w+) (?<capitalFigure>\\d+\\.\\d+|\\.\\d+|\\d+))");
    private static final Pattern ALT_EXTRACT = Pattern.compile(
            "(?i:(?<capitalDate>\\d+\\D\\d+\\D\\d+) (?:(?<capitalDesc>STATEMENT OF CAPITAL)|(?<capitalAltDesc>TREASURY CAPITAL)) (?<capitalCurrency>\\w+) (?<capitalFigure>\\d+\\.\\d+|\\.\\d+|\\d+))");
    public static final String DATA_DESCRIPTION_FIELD_PATH = "data.description";
    public static final String FIGURE = "1,000";
    private static final String ALT_DESCRIPTION = "capital-cancellation-treasury-shares-with-date-treasury-capital-figure";
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .registerModule(new JavaTimeModule());

    @InjectMocks
    private ProcessCapital processCapital;
    @Mock
    private FormatNumber formatNumber;

    @Test
    void shouldTransformCaptureGroupsIntoCapitalNode() {
        // given
        when(formatNumber.apply(any())).thenReturn(FIGURE);

        JsonNode source = buildSourceNode();

        JsonNode expected = buildExpectedNode();

        // when
        ObjectNode actual = source.deepCopy();
        processCapital.transform(source, actual, DATA_DESCRIPTION_FIELD_PATH, EXTRACT, null);

        // then
        assertEquals(expected, actual);
        verify(formatNumber).apply("1000");
        verify(formatNumber).apply("2000");
        verify(formatNumber).apply("3000");
    }

    @Test
    void shouldTransformAltCaptureGroupsIntoAltCapitalNode() {
        // given
        when(formatNumber.apply(any())).thenReturn(FIGURE);

        JsonNode source = buildSourceAltNode();

        JsonNode expected = buildExpectedAltNode();

        // when
        ObjectNode actual = source.deepCopy();
        processCapital.transform(source, actual, DATA_DESCRIPTION_FIELD_PATH, ALT_EXTRACT, ALT_DESCRIPTION);

        // then
        assertEquals(expected, actual);
        verify(formatNumber).apply("1000");
        verify(formatNumber).apply("50000");
        verify(formatNumber).apply("3333");
        verify(formatNumber).apply("221");
    }

    private static JsonNode buildSourceNode() {
        final ObjectNode topLevelNode = objectMapper.createObjectNode()
                .put("_entity_id", "3063732185")
                .put("_barcode", "XAITVXAX");

        topLevelNode
                .putObject("data")
                .put("type", "RP04SH02")
                .put("description",
                        """
                                Second filed SH02 - 03/02/16\s
                                Statement of Capital gbp 1000 03/02/16\s
                                Statement of Capital eur 2000 03/02/16 Statement of Capital usd 3000""");

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
                        """
                                13/05/14 Statement of Capital USD 1000\s
                                13/05/14 Treasury Capital GBP 50000\s
                                13/05/14 Statement of Capital HKD 3333\s
                                13/05/14 Statement of Capital JPY 221""");

        return topLevelNode;
    }

    private static JsonNode buildExpectedNode() {
        final ObjectNode topLevelNode = objectMapper.createObjectNode()
                .put("_entity_id", "3063732185")
                .put("_barcode", "XAITVXAX");

        ObjectNode capital1 = objectMapper.createObjectNode()
                .put("currency", "GBP")
                .put("figure", FIGURE);

        ObjectNode capital2 = objectMapper.createObjectNode()
                .put("currency", "EUR")
                .put("figure", FIGURE);

        ObjectNode capital3 = objectMapper.createObjectNode()
                .put("currency", "USD")
                .put("figure", FIGURE);

        topLevelNode
                .putObject("data")
                .put("type", "RP04SH02")
                .put("description", // this gets set to its enum value when applying the set rules
                        """
                                Second filed SH02 - 03/02/16\s
                                Statement of Capital gbp 1000 03/02/16\s
                                Statement of Capital eur 2000 03/02/16 Statement of Capital usd 3000""")
                .putObject("description_values")
                .putArray("capital")
                .add(capital1)
                .add(capital2)
                .add(capital3);

        return topLevelNode;
    }

    private static JsonNode buildExpectedAltNode() {
        final ObjectNode topLevelNode = objectMapper.createObjectNode()
                .put("_entity_id", "3081501305")
                .put("_barcode", "13CMU31L");

        topLevelNode
                .putObject("data")
                .put("type", "SH05")
                .put("description", // this gets set to its enum value when applying the set rules
                        """
                                13/05/14 Statement of Capital USD 1000\s
                                13/05/14 Treasury Capital GBP 50000\s
                                13/05/14 Statement of Capital HKD 3333\s
                                13/05/14 Statement of Capital JPY 221""");

        ObjectNode altCapital = objectMapper.createObjectNode()
                .put("currency", "GBP")
                // has a date because of 'treasury' within the description
                // has nothing to do with if it's an alt_capital or not
                .put("date", "13/05/14")
                .put("description", "capital-cancellation-treasury-shares-with-date-treasury-capital-figure")
                .put("figure", FIGURE);

        ObjectNode capital1 = objectMapper.createObjectNode()
                .put("currency", "USD")
                .put("figure", FIGURE);

        ObjectNode capital2 = objectMapper.createObjectNode()
                .put("currency", "HKD")
                .put("figure", FIGURE);

        ObjectNode capital3 = objectMapper.createObjectNode()
                .put("currency", "JPY")
                .put("figure", FIGURE);

        ObjectNode descriptionValues = ((ObjectNode) topLevelNode.at("/data")).putObject("description_values");

        descriptionValues
                .putArray("alt_capital")
                .add(altCapital);
        descriptionValues
                .putArray("capital")
                .add(capital1)
                .add(capital2)
                .add(capital3);

        return topLevelNode;
    }
}