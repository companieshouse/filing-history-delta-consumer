package uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform;

import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@Component
public class JsonNodeCleaner {

    private final ObjectMapper objectMapper;

    public JsonNodeCleaner(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    JsonNode setEmptyStringsToNull(JsonNode inputNode) {
        ObjectNode outputNode = (ObjectNode) inputNode;
        Map<String, Object> map = objectMapper.convertValue(outputNode, new TypeReference<>() {
        });
        map.forEach((key, value) -> {
            if ("".equals(value)) {
                outputNode.putNull(key);
            }
        });
        return outputNode;
    }
}
