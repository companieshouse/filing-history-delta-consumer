package uk.gov.companieshouse.filinghistory.consumer.transformers.parsers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

class ThenBuilder {

    @JsonProperty("set")
    private Map<String, String> set;
    @JsonProperty("define")
    private Map<?, ?> define;
    @JsonProperty("exec")
    private Map<?, ?> exec;

    public void compile() {
        // Parse the entries in the set, define and exec to:
        //  - set: build a map of field name -> one or more transformer functions
        //  - exec: ?? field name -> Call a custom method to build the value. Check Perl ??
        //  - define: ?? Apply regex to some field. Check Perl ??
    }


}
