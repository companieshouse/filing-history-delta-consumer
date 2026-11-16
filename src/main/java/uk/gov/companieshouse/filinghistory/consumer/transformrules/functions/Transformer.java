package uk.gov.companieshouse.filinghistory.consumer.transformrules.functions;

import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

public interface Transformer {

    void transform(JsonNode source,
            ObjectNode outputNode,
            String field,
            List<String> arguments,
            Map<String, String> contextValue);
}
