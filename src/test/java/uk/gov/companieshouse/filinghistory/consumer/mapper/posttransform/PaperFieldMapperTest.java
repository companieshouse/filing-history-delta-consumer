package uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PaperFieldMapperTest {

    private final PaperFiledMapper mapper = new PaperFiledMapper();

    @ParameterizedTest(name = "Test isPaperFiled should be {3} with barcode: {0}, formType: {1}, parentFormType: {2}")
    @CsvSource(value = {
            "XAITVXAX , null , AA , false",
            "TAITVXAX , null , AA , true",
            "null , null , AA , true",
            "'' , null , AA , true",
            "XAITVXAX , ANNOTATION , AA , false",
            "XAITVXAX , ANNOTATION , OTHER , false",
            "XAITVXAX , ANNOTATION , AAMD , false",
            "TAITVXAX , ANNOTATION , AA , false",
            "null , ANNOTATION , AA , false",
            "null , ANNOTATION , OTHER , true",
            "'' , ANNOTATION , AA , false",
            "'' , ANNOTATION , OTHER , true",
            "XAITVXAX , '' , AA , false",
            "TAITVXAX , '' , AA , true",
            "null , '' , AA , true",
            "'' , '' , AA , true",
            "XAITVXAX , ANNOTATION , null , false",
            "TAITVXAX , ANNOTATION , null , true",
            "null , ANNOTATION , null , true",
            "'' , ANNOTATION , null , true",
            
    },
            nullValues = {"null"})
    void testPaperFieldReturnsBooleanCorrectly(final String barcode, final String formType, final String parentFormType, final boolean expected) {
        // given

        // when
        final boolean actual = mapper.isPaperFiled(barcode, formType, parentFormType);

        // then
        assertEquals(expected, actual);
    }
}
