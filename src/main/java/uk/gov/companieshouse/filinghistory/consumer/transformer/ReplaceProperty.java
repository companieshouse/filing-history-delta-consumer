package uk.gov.companieshouse.filinghistory.consumer.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;

public class ReplaceProperty implements Transformer {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public JsonNode apply(JsonNode inputRequest,
            ObjectNode outputRequest,
            String field,
            List<String> arguments,
            Map<String, String> contextValue) {
        String[] fields = field.split("\\."); // len = 2
        ObjectNode root = outputRequest;
        for (int i = 0; i < fields.length - 1; i++) {
            JsonNode node = root.at("/" + fields[i]);
            if (node.getNodeType() == JsonNodeType.MISSING) {
                // Create a new node based on whether
                // 1. This is not a leaf node (i.e 'data') = i < fields.length - 2
                // 2. It is a text leaf node - i == fields.length - 1 AND arguments.size() == 1
                // 3. It is an array leaf node - i == fields.length - 1 AND arguments.size() == 1

//                if (arguments.size() == 1) {
//                    node = new TextNode(arguments.getFirst());
//                } else {
//                    node = objectMapper.createArrayNode();
//                    root.

                //node = new ObjectNode()
            }
            root = (ObjectNode) node;
        }
        // root == /data

        // root.put(fields[fields.length - 1], new value -- text node | array node)

//        ((ObjectNode)outputRequest.at("/data/category")).
//                data.category: insolvency
        return outputRequest;
    }
}
