package uk.gov.companieshouse.filinghistory.consumer.transformers;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

// Container for Rules - TBD
@Component
public record TransformRule(When when /* TODO and set etc*/) {

    public boolean match(JsonNode putRequest) {
        return when.match(putRequest);
    }

    public void apply(JsonNode putRequest) {
        // Apply define functions
        // Apply setters
        // Apply exec functions
    }
}
