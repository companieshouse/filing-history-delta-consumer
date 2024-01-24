package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class LowerCase implements Transformer {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void transform(JsonNode source,
            ObjectNode outputNode,
            String field,
            List<String> arguments,
            Map<String, String> contextValue) {

        String finalField = getFinalField(objectMapper, field, outputNode);

        outputNode.put(finalField, "TODO: Lower case: " + arguments.getFirst());
    }

     String transformLowerCase(String nodeText){
        if(StringUtils.isBlank(nodeText)){
            return nodeText;
        }
        return nodeText.toLowerCase();
    }
}
