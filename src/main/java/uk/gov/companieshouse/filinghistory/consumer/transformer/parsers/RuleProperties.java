package uk.gov.companieshouse.filinghistory.consumer.transformer.parsers;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.filinghistory.consumer.transformer.TransformRule;

public class RuleProperties {

    @JsonProperty("when")
    private WhenProperties when;
    @JsonProperty("then")
    private ThenProperties then;
    @JsonProperty("default")
    private DefaultProperties defaultRule;

    public TransformRule compile() {
        if (defaultRule != null) {
            return new TransformRule(defaultRule.compile());
        } else {
            return new TransformRule(when.compile(), then.compile());
        }
    }
}
