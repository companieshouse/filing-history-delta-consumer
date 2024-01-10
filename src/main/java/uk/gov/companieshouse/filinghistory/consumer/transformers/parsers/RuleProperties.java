package uk.gov.companieshouse.filinghistory.consumer.transformers.parsers;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.filinghistory.consumer.transformers.TransformRule;

public class RuleProperties {

    @JsonProperty("when")
    private WhenProperties when;
    @JsonProperty("then")
    private ThenBuilder then;
    @JsonProperty("default")
    private DefaultBuilder defaultRule;

    public TransformRule compile() {
        if (when != null) {
            when.compile();
        }

        if (then != null) {
            then.compile();
        }

        if (defaultRule != null) {
            defaultRule.compile();
        }
    }
}
