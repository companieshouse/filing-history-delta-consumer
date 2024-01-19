package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public record Then(Map<String, Pattern> define,
                   Map<String, SetterArgs> setters,
                   Map<String, List<String>> exec) {

    public JsonNode apply(JsonNode putRequest, Map<String, String> contextData) {
        ObjectNode updated = putRequest.deepCopy();
        // 1. Process define elements to add to the contextData
        // TODO

        // 2. Setters
        setters.forEach(
                (key, value) -> value.transformer()
                        .transform(putRequest, updated, key, value, contextData));

        // 3. Exec. field name -> Call a custom method to build the value. Check Perl ??
        // TODO

        return updated;
    }


}
