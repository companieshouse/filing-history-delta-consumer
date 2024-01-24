package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface Transformer {

    void transform(JsonNode source,
            ObjectNode outputNode,
            String field,
            List<String> arguments,
            Map<String, String> contextValue);

    default String getFinalField(ObjectMapper objectMapper, String field, ObjectNode outputNode){
        String[] fields = field.split("\\."); // len = 2
        for (int i = 0; i < fields.length - 1; i++) {
            outputNode.putIfAbsent(fields[i], objectMapper.createObjectNode());
            outputNode = (ObjectNode) outputNode.at("/" + fields[i]);
        }

        return fields[fields.length - 1];
    }
}
