package uk.gov.companieshouse.filinghistory.consumer.transformrules.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.FormatNumber;

class FormatNumberTest {

    private final FormatNumber formatNumber = new FormatNumber();

    @ParameterizedTest
    @MethodSource("inputNumberStringsAndExpectedNumberStrings")
    void shouldFormatNumberWithCommasDelimitingThousands(String numberToFormat, String expected) throws ParseException {
        // given

        // when
        String actual = formatNumber.apply(numberToFormat);

        // then
        assertEquals(expected, actual);
    }

    private static Stream<Arguments> inputNumberStringsAndExpectedNumberStrings() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("", ""),
                Arguments.of("0", "0"),
                Arguments.of("1", "1"),
                Arguments.of("12", "12"),
                Arguments.of("123", "123"),
                Arguments.of("1234", "1,234"),
                Arguments.of("12345", "12,345"),
                Arguments.of("12345.67", "12,345.67"),
                Arguments.of("123.45", "123.45"),
                Arguments.of("1234567.89", "1,234,567.89"),
                Arguments.of("1234.1234", "1,234.1234"),
                Arguments.of("32533136.894660", "32,533,136.894660"));
    }
}