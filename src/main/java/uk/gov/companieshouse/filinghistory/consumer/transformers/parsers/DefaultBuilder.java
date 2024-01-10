package uk.gov.companieshouse.filinghistory.consumer.transformers.parsers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import uk.gov.companieshouse.filinghistory.consumer.transformers.Default;

class DefaultBuilder {

    @JsonProperty("set")
    private Map<String, String> set;

    public Default compile() {
        return new Default();
    }
}
