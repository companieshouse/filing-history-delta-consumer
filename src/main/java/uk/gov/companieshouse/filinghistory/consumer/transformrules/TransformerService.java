package uk.gov.companieshouse.filinghistory.consumer.transformrules;

import static uk.gov.companieshouse.filinghistory.consumer.Application.NAMESPACE;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.rules.Result;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.rules.Rule;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.rules.When;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

public class TransformerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
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
                When when = rule.when();
                LOGGER.info("Transaction %s, matched transform rule: [eq: %s, like: %s]"
                        .formatted(delta.get("_entity_id").textValue(), when.formType(), when.like()));
                return rule.apply(delta, result.contextData());
            }
        }
        return defaultRule.apply(delta, Map.of());
    }
}
