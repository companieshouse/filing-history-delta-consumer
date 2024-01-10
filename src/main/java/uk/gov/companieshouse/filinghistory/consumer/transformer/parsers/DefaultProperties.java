package uk.gov.companieshouse.filinghistory.consumer.transformer.parsers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import uk.gov.companieshouse.filinghistory.consumer.transformer.Default;

class DefaultProperties {

    @JsonProperty("set")
    private Map<String, String> set;

    public Default compile() {
        return new Default();
    }
}
