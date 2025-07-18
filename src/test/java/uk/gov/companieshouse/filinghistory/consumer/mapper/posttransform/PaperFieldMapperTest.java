package uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PaperFieldMapperTest {

    private final PaperFiledMapper mapper = new PaperFiledMapper();

    @ParameterizedTest(name = "Test isPaperFiled should be {2} with barcode: {0}, formType: {1}")
    @CsvSource(value = {
            "XAITVXAX , null , false",
            "TAITVXAX , null , true",
            "null , null , true",
            "'' , null , true",
            "XAITVXAX , ANNOTATION , false",
            "TAITVXAX , ANNOTATION , false",
            "null , ANNOTATION , false",
            "'' , ANNOTATION , false",
            "XAITVXAX , '' , false",
            "TAITVXAX , '' , true",
            "null , '' , true",
            "'' , '' , true",
            
    },
            nullValues = {"null"})
    void testPaperFieldReturnsBooleanCorrectly(final String barcode, final String formType, final boolean expected) {
        // given

        // when
        final boolean actual = mapper.isPaperFiled(barcode, formType);

        // then
        assertEquals(expected, actual);
    }
}
