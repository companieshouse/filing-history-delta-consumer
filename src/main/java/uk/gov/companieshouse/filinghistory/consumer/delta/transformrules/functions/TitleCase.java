package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.rules.SetterArgs;

public class TitleCase implements Transformer {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void transform(JsonNode source,
            ObjectNode outputNode,
            String field,
            SetterArgs setterArgs,
            Map<String, String> contextValue) {

        String[] fields = field.split("\\."); // len = 2
        for (int i = 0; i < fields.length - 1; i++) {
            outputNode.putIfAbsent(fields[i], objectMapper.createObjectNode());
            outputNode = (ObjectNode) outputNode.at("/" + fields[i]);
        }

        String finalField = fields[fields.length - 1];

        outputNode.put(finalField, "TODO: Title case: " + setterArgs.arguments().getFirst());
    }
}
