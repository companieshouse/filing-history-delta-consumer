package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.parsers;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.rules.Rule;

public record RuleProperties(@JsonProperty("when") WhenProperties when,
                             @JsonProperty("then") ThenProperties then,
                             @JsonProperty("default") DefaultProperties defaultRule) {

    public Rule compile() {
        if (defaultRule != null) {
            return new Rule(defaultRule.compile());
        } else {
            return new Rule(when.compile(), then.compile());
        }
    }
}
