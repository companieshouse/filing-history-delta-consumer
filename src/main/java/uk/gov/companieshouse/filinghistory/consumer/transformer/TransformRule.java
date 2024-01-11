package uk.gov.companieshouse.filinghistory.consumer.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;

// Container for Rules - TBD
public record TransformRule(When when, Then then, Default defaultRule) {

    public TransformRule(Default defaultRule) {
        this(null, null, defaultRule);
    }

    public TransformRule(When when, Then then) {
        this(when, then, null);
    }

    public Result match(JsonNode putRequest) {
        return when.match(putRequest);
    }

    public JsonNode apply(JsonNode putRequest, Map<String, String> captureGroups) {
        return then.apply(putRequest, captureGroups);
    }
}
