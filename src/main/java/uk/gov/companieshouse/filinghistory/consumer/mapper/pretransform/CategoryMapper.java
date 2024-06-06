package uk.gov.companieshouse.filinghistory.consumer.mapper.pretransform;

import java.util.Map;
import org.springframework.stereotype.Component;

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

    String map(String category) {
        return CATEGORIES.getOrDefault(category, category);
    }
}
