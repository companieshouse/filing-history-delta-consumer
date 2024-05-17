package uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PaperFieldMapperTest {

    private final PaperFiledMapper mapper = new PaperFiledMapper();

    @ParameterizedTest
    @CsvSource(value = {
            "XAITVXAX , false",
            "TAITVXAX , true",
            "null , true",
            "'' , true"
    },
            nullValues = {"null"})
    void testPaperFieldReturnsBooleanCorrectly(final String barcode, final boolean expected) {
        // given

        // when
        final boolean actual = mapper.isPaperFiled(barcode);

        // then
        assertEquals(expected, actual);
    }
}
