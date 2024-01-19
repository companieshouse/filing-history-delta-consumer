package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.rules.Result;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.rules.Rule;

public class TransformerService {

    private final Rule defaultRule;
    private final List<Rule> compiledRules;

    public TransformerService(Rule defaultRule, List<Rule> compiledRules) {
        this.defaultRule = defaultRule;
        this.compiledRules = compiledRules;
    }

    public JsonNode transform(JsonNode delta) {
        for (Rule rule : compiledRules) {
            Result result = rule.match(delta);
            if (result.matched()) {
                return rule.apply(delta, result.contextData());
            }
        }
        return defaultRule.apply(delta, Map.of());
    }
}
