package uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData.CategoryEnum;

@Component
public class CategoryMapper {

    private static final Map<String, String> CATEGORIES = Map.ofEntries(
            Map.entry("0", "accounts"),
            Map.entry("1", "return"),
            Map.entry("2", "officer"),
            Map.entry("3", "address"),
            Map.entry("4", "mortgage"),
            Map.entry("5", "liquidation"),
            Map.entry("6", "incorporation"),
            Map.entry("7", "capital"),
            Map.entry("8", "change-of-name"),
            Map.entry("9", "miscellaneous"),
            Map.entry("10", "persons-with-significant-control")
    );

    CategoryEnum map(JsonNode node) {
        return Optional.ofNullable(node)
                .map(n -> n.get("category"))
                .map(JsonNode::textValue)
                .map(value -> CATEGORIES.getOrDefault(value, value))
                .map(CategoryEnum::fromValue)
                .orElse(null);
    }
}
