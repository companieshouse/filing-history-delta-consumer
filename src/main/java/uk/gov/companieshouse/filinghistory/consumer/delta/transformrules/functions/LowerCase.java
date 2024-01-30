package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.eclipse.jetty.util.StringUtil;
import org.springframework.stereotype.Component;
@Component
public class LowerCase implements Transformer {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Pattern MIXED_ALPHANUMERIC = Pattern.compile("(\\w+\\d+\\w*|\\d+\\w+)");
    @Override
    public void transform(JsonNode source,
            ObjectNode outputNode,
            String field,
            List<String> arguments,
            Map<String, String> contextValue) {

        String finalField = getFinalField(objectMapper, field, outputNode);

        outputNode.put(finalField, "TODO: Lower case: " + arguments.getFirst());
    }

    public String transformLowerCase(String nodeText){
        if(StringUtil.isEmpty(nodeText)){
            return nodeText;
        }
        nodeText = Transformer.mapToken(MIXED_ALPHANUMERIC, nodeText, (token, matcher) ->
                matcher.group(1).toLowerCase(Locale.UK), true);
        return nodeText.trim();
    }

}
