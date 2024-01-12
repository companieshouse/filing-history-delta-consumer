package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.parsers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.rules.Default;

class DefaultProperties {

    @JsonProperty("set")
    private Map<String, String> set;

    public Default compile() {
        return new Default();
    }
}
