package uk.gov.companieshouse.filinghistory.consumer.transformrules.parsers;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.TransformerFactory;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.rules.Rule;

public record RuleProperties(@JsonProperty("when") WhenProperties when,
                             @JsonProperty("then") ThenProperties then,
                             @JsonProperty("default") DefaultProperties defaultRule) {

    public Rule compile(TransformerFactory transformerFactory) {
        if (defaultRule != null) {
            return new Rule(defaultRule.compile(transformerFactory));
        } else {
            return new Rule(when.compile(), then.compile(transformerFactory));
        }
    }
}
