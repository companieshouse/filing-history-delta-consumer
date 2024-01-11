package uk.gov.companieshouse.filinghistory.consumer.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface Transformer {

    JsonNode apply(JsonNode inputRequest,
            ObjectNode outputRequest,
            String field,
            List<String> arguments,
            Map<String, String> contextValue);
}
