package uk.gov.companieshouse.filinghistory.consumer.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public record Then(Map<String, Pattern> define,
                   Map<String, SetterArgs> setters,
                   Map<String, List<String>> exec) {

    public JsonNode apply(JsonNode putRequest, Map<String, String> contextData) {
        JsonNode updated = putRequest.deepCopy();
        // 1. Process define elements to add to the contextData

        // 2. Setters
        setters.entrySet()
                .forEach(e -> e.getValue()
                        .transformer()
                        .apply(putRequest, updated, e.getKey(), e.getValue(), contextData));


        // Parse the entries in the set, define and exec to:
        //  - set: build a map of field name -> one or more transformer functions
        //  - exec: ?? field na.e -> Call a custom method to build the value. Check Perl ??
        //  - define: ?? Apply regex to some field. Check Perl ??
        return updated;
    }


}
