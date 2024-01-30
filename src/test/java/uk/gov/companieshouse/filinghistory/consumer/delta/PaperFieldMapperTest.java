package uk.gov.companieshouse.filinghistory.consumer.delta;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PaperFieldMapperTest {

    private final PaperFiledMapper mapper = new PaperFiledMapper();

    @ParameterizedTest
    @CsvSource(value = {
            "XAITVXAX , 000XAITVXAX4682 , false",
            "TAITVXAX , 000XAITVXAX4682 , true",
            "XAITVXAX , 000TAITVXAX4682 , false",
            "TAITVXAX , 000TAITVXAX4682 , true",
            "null , 000XAITVXAX4682 , false",
            "null , 000TAITVXAX4682 , true",
            "XAITVXAX , null , false",
            "TAITVXAX , null , true",
            "null , null , true",
            "'' , 000XAITVXAX4682 , false",
            "'' , 000TAITVXAX4682 , true",
            "XAITVXAX , '' , false",
            "TAITVXAX , '' , true",
            "'' , '' , true",
    },
    nullValues = {"null"})
    void testPaperFieldReturnsBooleanCorrectly(final String barcode, final String documentId, final boolean expected) {
        // given

        // when
        final boolean actual = mapper.map(barcode, documentId);

        // then
        assertEquals(expected, actual);
    }
}
