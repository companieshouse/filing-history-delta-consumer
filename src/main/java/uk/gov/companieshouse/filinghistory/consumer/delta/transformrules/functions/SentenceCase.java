package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.rules.SetterArgs;

public class SentenceCase implements Transformer {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void transform(JsonNode source,
            ObjectNode outputNode,
            String field,
            SetterArgs setterArgs,
            Map<String, String> contextValue) {

        ObjectNode workingNode = outputNode;

        String[] fields = field.split("\\."); // len = 2
        for (int i = 0; i < fields.length - 1; i++) {
            workingNode.putIfAbsent(fields[i], objectMapper.createObjectNode());
            workingNode = (ObjectNode) workingNode.at("/" + fields[i]);
        }

        String finalField = fields[fields.length - 1];
        String nodeText = outputNode.at("/" + setterArgs.arguments().getFirst().replace(".", "/"))
                .textValue();

        // TODO Apply Perl sentence_case transformation to node text
        String transformedText = "TODO: Sentence case: " + nodeText;

        outputNode.put(finalField, transformedText);
    }
}
