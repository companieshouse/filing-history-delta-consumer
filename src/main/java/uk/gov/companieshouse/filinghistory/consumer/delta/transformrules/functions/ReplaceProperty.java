package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ReplaceProperty implements Transformer {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void transform(JsonNode source,
            ObjectNode outputNode,
            String field,
            List<String> arguments,
            Map<String, String> contextValue) {

        String finalField = getFinalField(objectMapper, field, outputNode);

        if (arguments.size() == 1) {

//            TODO
//            data.category: accounts tick
//            data.description: 'accounts-with-accounts-type-[% accounts_type | lc %]-group'
//            original_description: '[% data.description | sentence_case %]'
//            data.action_date: '[% made_up_date | bson_date %]'

            outputNode.put(finalField, arguments.getFirst());
        } else {
            ArrayNode leafNode = outputNode.putArray(finalField);
//          TODO test with a unit test using line 134 values of transform_rules.yml
//           to replace value with an array instead of a string.
            arguments.forEach(leafNode::add);
        }
    }
}
