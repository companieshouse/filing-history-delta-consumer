package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
import org.eclipse.jetty.util.StringUtil;
import org.springframework.stereotype.Component;
@Component
public class BsonDate implements Transformer {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void transform(JsonNode source,
            ObjectNode outputNode,
            String field,
            List<String> arguments,
            Map<String, String> contextValue) {

        String finalField = getFinalField(objectMapper, field, outputNode);

        outputNode.put(finalField, "TODO: BSON date: " + arguments.getFirst());
    }

    public String transformBsonDate(String nodeText){
        if(StringUtil.isEmpty(nodeText)){
            return nodeText;
        }


        return nodeText.trim();
    }

}
