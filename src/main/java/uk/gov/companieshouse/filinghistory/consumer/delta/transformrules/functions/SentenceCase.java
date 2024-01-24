package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;

public class SentenceCase implements Transformer {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void transform(JsonNode source,
            ObjectNode outputNode,
            String field,
            List<String> arguments,
            Map<String, String> contextValue) {

        ObjectNode workingNode = outputNode;

        String finalField = getFinalField(objectMapper, field, outputNode);
        String nodeText = outputNode.at("/" + arguments.getFirst().replace(".", "/"))
                .textValue();

        // TODO Apply Perl sentence_case transformation to node text
        String transformedText = "TODO: Sentence case: " + nodeText;

        outputNode.put(finalField, transformedText);
    }
}
