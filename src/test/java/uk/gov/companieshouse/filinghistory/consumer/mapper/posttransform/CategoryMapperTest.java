package uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.companieshouse.api.filinghistory.ExternalData.CategoryEnum;

class CategoryMapperTest {

    private final CategoryMapper categoryMapper = new CategoryMapper();
    private final ObjectMapper objectMapper = new ObjectMapper();

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
    void shouldGetCorrectCategoryEnumFromNodeValueWhenNumber(final String nodeValue, final String category) {
        // given
        JsonNode node = objectMapper.createObjectNode()
                .put("category", nodeValue);

        final CategoryEnum expected = CategoryEnum.fromValue(category);

        // when
        final CategoryEnum actual = categoryMapper.map(node);

        // then
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({
            "accounts",
            "address",
            "annotation",
            "annual-return",
            "auditors",
            "capital",
            "certificate",
            "change-of-constitution",
            "change-of-name",
            "confirmation-statement",
            "court-order",
            "dissolution",
            "document-replacement",
            "gazette",
            "historical",
            "incorporation",
            "insolvency",
            "liquidation",
            "miscellaneous",
            "mortgage",
            "officer",
            "officers",
            "other",
            "persons-with-significant-control",
            "reregistration",
            "resolution",
            "restoration",
            "return"
    })
    void shouldGetCorrectCategoryEnumFromNodeValue(final String nodeValue) {
        // given
        JsonNode node = objectMapper.createObjectNode()
                .put("category", nodeValue);

        final CategoryEnum expected = CategoryEnum.fromValue(nodeValue);

        // when
        final CategoryEnum actual = categoryMapper.map(node);

        // then
        assertEquals(expected, actual);
    }
}
