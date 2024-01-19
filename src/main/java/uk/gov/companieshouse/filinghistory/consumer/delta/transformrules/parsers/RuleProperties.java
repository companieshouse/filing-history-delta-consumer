package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.parsers;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.rules.Rule;

public class RuleProperties {

    @JsonProperty("when")
    private WhenProperties when;
    @JsonProperty("then")
    private ThenProperties then;
    @JsonProperty("default")
    private DefaultProperties defaultRule;

    public Rule compile() {
        if (defaultRule != null) {
            return new Rule(defaultRule.compile());
        } else {
            return new Rule(when.compile(), then.compile());
        }
    }
}
