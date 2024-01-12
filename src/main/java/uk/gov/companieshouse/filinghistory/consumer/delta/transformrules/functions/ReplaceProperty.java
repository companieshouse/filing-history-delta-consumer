package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.rules.SetterArgs;

public class ReplaceProperty implements Transformer {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void transform(ObjectNode rootNode,
            String field,
            SetterArgs setterArgs,
            Map<String, String> contextValue) {

        String[] fields = field.split("\\."); // len = 2
        for (int i = 0; i < fields.length - 1; i++) {
            rootNode.putIfAbsent(fields[i], objectMapper.createObjectNode());
            rootNode = (ObjectNode) rootNode.at("/" + fields[i]);
        }

        String finalField = fields[fields.length - 1];

        if (setterArgs.arguments().size() == 1) {
            rootNode.put(finalField, setterArgs.arguments().getFirst());
        } else {
            ArrayNode leafNode = rootNode.putArray(finalField);
            setterArgs.arguments().forEach(leafNode::add);
        }
    }
}
