package uk.gov.companieshouse.filinghistory.consumer.mapper.pretransform;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CategoryMapperTest {

    private final CategoryMapper categoryMapper = new CategoryMapper();

    @ParameterizedTest
    @CsvSource({
            "0 , accounts",
            "1 , return",
            "2 , officer",
            "3 , address",
            "4 , mortgage",
            "5 , liquidation",
            "6 , incorporation",
            "7 , capital",
            "8 , change-of-name",
            "9 , miscellaneous",
            "10 , persons-with-significant-control"
    })
    void shouldReturnCategoryFromKey(final String key, final String expected) {
        // when
        final String actual = categoryMapper.map(key);

        // then
        assertEquals(expected, actual);
    }
}
