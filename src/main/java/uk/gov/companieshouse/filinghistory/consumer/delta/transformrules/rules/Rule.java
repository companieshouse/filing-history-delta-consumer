package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.rules;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;

// Container for Rules - TBD
public record Rule(When when, Then then, Default defaultRule) {

    public Rule(Default defaultRule) {
        this(null, null, defaultRule);
    }

    public Rule(When when, Then then) {
        this(when, then, null);
    }

    public Result match(JsonNode putRequest) {
        return when.match(putRequest);
    }

    public JsonNode apply(JsonNode putRequest, Map<String, String> captureGroups) {
        return then.apply(putRequest, captureGroups);
    }
}