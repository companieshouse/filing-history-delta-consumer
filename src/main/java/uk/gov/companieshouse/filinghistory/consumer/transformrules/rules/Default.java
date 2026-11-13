package uk.gov.companieshouse.filinghistory.consumer.transformrules.rules;

import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

public record Default(Map<String, SetterArgs> setters) {

    public JsonNode apply(JsonNode source, Map<String, String> contextData) {
        ObjectNode updated = (ObjectNode) source.deepCopy();

        setters.forEach(
                (key, value) -> value.transformer()
                        .transform(source, updated, key, value.arguments(), contextData));

        return updated;
    }
}
