package uk.gov.companieshouse.filinghistory.consumer.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.companieshouse.api.filinghistory.ExternalData.CategoryEnum;

@SpringBootTest
class CategoryMapperTest {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ObjectMapper objectMapper;

    @ParameterizedTest
    @CsvSource({
            "0 , accounts",
            "1 , return", // TODO: FIXME - "return" not matching a category
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
        final CategoryEnum actual = categoryMapper.map(node, CategoryEnum::fromValue);

        // then
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({
            "accounts",
            "address",
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
            "restoration"
    })
    void shouldGetCorrectCategoryEnumFromNodeValue(final String nodeValue) {
        // given
        JsonNode node = objectMapper.createObjectNode()
                .put("category", nodeValue);

        final CategoryEnum expected = CategoryEnum.fromValue(nodeValue);

        // when
        final CategoryEnum actual = categoryMapper.map(node, CategoryEnum::fromValue);

        // then
        assertEquals(expected, actual);
    }
}
