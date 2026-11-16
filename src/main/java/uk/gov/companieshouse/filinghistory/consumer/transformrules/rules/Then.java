package uk.gov.companieshouse.filinghistory.consumer.transformrules.rules;

import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

public record Then(Map<String, SetterArgs> setters, ExecArgs execArgs) {

    public JsonNode apply(JsonNode source, Map<String, String> contextData) {
        ObjectNode updated = (ObjectNode) source.deepCopy();

        setters.forEach(
                (key, value) -> value.transformer()
                        .transform(source, updated, key, value.arguments(), contextData));

        if (execArgs != null) {
            execArgs.processCapital().transform(source, updated, execArgs.fieldPath(),
                    execArgs.extract(), execArgs.altDescription());
        }

        return updated;
    }
}
