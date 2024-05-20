package uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class JsonNodeCleaner {

    private final ObjectMapper objectMapper;

    public JsonNodeCleaner(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    JsonNode setEmptyStringsToNull(JsonNode inputNode) {
        ObjectNode outputNode = (ObjectNode) inputNode;
        Map<?, ?> map = objectMapper.convertValue(outputNode, Map.class);
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if ("".equals(entry.getValue())) {
                outputNode.set(entry.getKey().toString(), null);
            }
        }
        return outputNode;
    }
}
