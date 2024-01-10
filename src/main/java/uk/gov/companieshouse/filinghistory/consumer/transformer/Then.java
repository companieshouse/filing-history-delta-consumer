package uk.gov.companieshouse.filinghistory.consumer.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

public record Then(Map<String, Pattern> define,
                   Map<String, BiFunction<JsonNode, String, String>> setters,
                   Map<String, List<String>> exec) {

    public JsonNode apply(JsonNode putRequest, Map<String, String> captureGroups) {
        JsonNode updated = putRequest.deepCopy();

        // Parse the entries in the set, define and exec to:
        //  - set: build a map of field name -> one or more transformer functions
        //  - exec: ?? field na.e -> Call a custom method to build the value. Check Perl ??
        //  - define: ?? Apply regex to some field. Check Perl ??
        return updated;
    }


}
