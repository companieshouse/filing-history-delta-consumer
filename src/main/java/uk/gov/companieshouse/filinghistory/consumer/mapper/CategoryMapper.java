package uk.gov.companieshouse.filinghistory.consumer.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.ExternalData.CategoryEnum;

@Component
public class CategoryMapper {

    private final Map<String, String> categories;

    public CategoryMapper(Map<String, String> categories) {
        this.categories = categories;
    }

    CategoryEnum map(JsonNode node) {
        return Optional.ofNullable(node)
                .map(n -> n.get("category"))
                .map(JsonNode::textValue)
                .map(value -> categories.getOrDefault(value, value))
                .map(CategoryEnum::fromValue)
                .orElse(null);
    }
}
