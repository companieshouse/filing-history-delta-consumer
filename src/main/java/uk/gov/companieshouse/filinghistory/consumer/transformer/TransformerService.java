package uk.gov.companieshouse.filinghistory.consumer.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;

public class TransformerService {

    private final TransformRule defaultRule;
    private final List<TransformRule> compiledRules;

    public TransformerService(TransformRule defaultRule, List<TransformRule> compiledRules) {
        this.defaultRule = defaultRule;
        this.compiledRules = compiledRules;
    }

    public JsonNode transform(JsonNode delta) {
        for (TransformRule rule : compiledRules) {
            Result result = rule.match(delta);
            if (result.matched()) {
                return rule.apply(delta, result.groups());
            }
        }
        return defaultRule.apply(delta, Map.of());
    }
}
