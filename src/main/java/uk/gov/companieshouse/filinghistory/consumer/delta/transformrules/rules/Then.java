package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;

public record Then(Map<String, SetterArgs> setters, ExecArgs execArgs) {

    public JsonNode apply(JsonNode source, Map<String, String> contextData) {
        ObjectNode updated = source.deepCopy();

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
