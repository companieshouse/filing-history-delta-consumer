package uk.gov.companieshouse.filinghistory.consumer.mapper;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CategoryConfig {

    @Bean
    public CategoryMapper categoryMapper() {
        return new CategoryMapper(Map.ofEntries(
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
                Map.entry("10", "persons-with-significant-control"))
        );
    }
}
