package uk.gov.companieshouse.filinghistory.consumer.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    private final Map<String, String> categories;

    public CategoryMapper(Map<String, String> categories) {
        this.categories = categories;
    }

    <T extends Enum<?>> T map(JsonNode node, Function<String, T> fromValue) {
        return Optional.ofNullable(node)
                .map(n -> n.get("category"))
                .map(JsonNode::textValue)
                .map(value -> categories.getOrDefault(value, value))
                .map(fromValue)
                .orElse(null);
    }
}
