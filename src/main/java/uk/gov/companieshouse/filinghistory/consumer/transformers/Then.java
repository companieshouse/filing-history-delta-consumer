package uk.gov.companieshouse.filinghistory.consumer.transformers;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.function.BiFunction;

public record Then(Map<String, BiFunction<JsonNode, String, String>> setters) {

    public void apply() {
        // Parse the entries in the set, define and exec to:
        //  - set: build a map of field name -> one or more transformer functions
        //  - exec: ?? field na.e -> Call a custom method to build the value. Check Perl ??
        //  - define: ?? Apply regex to some field. Check Perl ??
    }


}
