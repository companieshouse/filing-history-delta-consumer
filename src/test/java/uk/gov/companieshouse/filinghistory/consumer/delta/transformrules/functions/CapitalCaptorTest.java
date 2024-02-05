package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
class CapitalCaptorTest {

    private static final Pattern EXTRACT = Pattern.compile(
            "(?i:\\bSTATEMENT OF CAPITAL[; ](?<capitalCurrency>\\w+) (?<capitalFigure>\\d+\\.\\d+|\\.\\d+|\\d+))");
    private static final Pattern ALT_EXTRACT = Pattern.compile(
            "(?i:(?<capitalDate>\\d+\\D\\d+\\D\\d+) (?:(?<capitalDesc>STATEMENT OF CAPITAL)|(?<capitalAltDesc>TREASURY CAPITAL)) (?<capitalCurrency>\\w+) (?<capitalFigure>\\d+\\.\\d+|\\.\\d+|\\d+))");
    public static final String DATA_DESCRIPTION_FIELD_PATH = "data.description";
    public static final String FIGURE = "1,000";
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

    private CapitalCaptor capitalCaptor;
    @Mock
    private FormatNumber formatNumber;

    @BeforeEach
    void setUp() {
        capitalCaptor = new CapitalCaptor(TransformerTestingUtils.getMapper(), formatNumber);
    }

    @Test
    void shouldCaptureCapitalFieldsAndReturnCapturesArrayNode() {
        // given
        when(formatNumber.apply(any())).thenReturn(FIGURE);
        CapitalCaptures expected = buildExpected();

        // when
        CapitalCaptures actual = capitalCaptor.captureCapital(EXTRACT, null, SOURCE_DESCRIPTION);

        // then
        assertEquals(expected, actual);
        verify(formatNumber).apply("1000");
        verify(formatNumber).apply("2000");
        verify(formatNumber).apply("3000");
    }

    @Test
    void shouldCaptureCapitalAndAltCapitalFieldsAndReturnCapturesAndAltCapturesArrayNodes() {
        // given
        when(formatNumber.apply(any())).thenReturn(FIGURE);
        CapitalCaptures expected = buildExpectedAlt();

        // when
        CapitalCaptures actual = capitalCaptor.captureCapital(ALT_EXTRACT, ALT_DESCRIPTION, ALT_SOURCE_DESCRIPTION);

        // then
        assertEquals(expected, actual);
        verify(formatNumber).apply("1000");
        verify(formatNumber).apply("50000");
        verify(formatNumber).apply("3333");
        verify(formatNumber).apply("221");
    }

    private static CapitalCaptures buildExpected() {
        ObjectNode capital1 = objectMapper.createObjectNode()
                .put("currency", "GBP")
                .put("figure", FIGURE);

        ObjectNode capital2 = objectMapper.createObjectNode()
                .put("currency", "EUR")
                .put("figure", FIGURE);

        ObjectNode capital3 = objectMapper.createObjectNode()
                .put("currency", "USD")
                .put("figure", FIGURE);

        ArrayNode captures = objectMapper.createArrayNode()
                .add(capital1)
                .add(capital2)
                .add(capital3);

        return new CapitalCaptures(captures, objectMapper.createArrayNode());
    }

    private static CapitalCaptures buildExpectedAlt() {
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

        ArrayNode captures = objectMapper.createArrayNode()
                .add(capital1)
                .add(capital2)
                .add(capital3);

        ArrayNode altCaptures = objectMapper.createArrayNode()
                .add(altCapital);

        return new CapitalCaptures(captures, altCaptures);
    }
}