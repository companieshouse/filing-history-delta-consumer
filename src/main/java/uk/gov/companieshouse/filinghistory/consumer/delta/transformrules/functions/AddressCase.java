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
public class AddressCase implements Transformer {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final TitleCase titleCase = new TitleCase();
    private static final Pattern POST_CODE_PATTERN = Pattern.compile(
            "(\\b[A-Z][A-Z]?\\d[A-Z\\d]?\\s*\\d[A-Z]{2}\\b)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PO_BOX_PATTERN = Pattern.compile("\\bPo\\s+Box\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern NUMBER_SUFFIX_PATTERN = Pattern.compile("(\\b\\d+\\s*(nd|th|rd|st)\\b)", Pattern.CASE_INSENSITIVE);

    @Override
    public void transform(JsonNode source,
            ObjectNode outputNode,
            String field,
            List<String> arguments,
            Map<String, String> contextValue) {

        String finalField = getFinalField(objectMapper, field, outputNode);

        outputNode.put(finalField, "TODO: Address case: " + arguments.getFirst());
    }

    String transformAddressCase(String nodeText){
        if(StringUtil.isEmpty(nodeText)){
            return nodeText;
        }
        nodeText = titleCase.transformTitleCase(nodeText);
        nodeText = Transformer.mapToken(POST_CODE_PATTERN, nodeText, (word, matcher)
                -> matcher.group(1).toUpperCase(Locale.UK), true);
        nodeText = PO_BOX_PATTERN.matcher(nodeText).replaceFirst("PO Box");
        nodeText = Transformer.mapToken(NUMBER_SUFFIX_PATTERN, nodeText, (word, matcher)
                -> matcher.group(1).toLowerCase(Locale.UK), true);
        return nodeText.trim();
    }

}
