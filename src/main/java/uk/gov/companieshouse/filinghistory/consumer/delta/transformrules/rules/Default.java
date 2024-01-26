package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;

public record Default(Map<String, SetterArgs> setters) {

    public JsonNode apply(JsonNode source, Map<String, String> contextData) {
        ObjectNode updated = source.deepCopy();

        setters.forEach(
                (key, value) -> value.transformer()
                        .transform(source, updated, key, value, contextData));

        return updated;
    }
}
