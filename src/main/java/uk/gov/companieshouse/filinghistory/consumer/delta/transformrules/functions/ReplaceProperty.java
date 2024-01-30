package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import static uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.TransformerUtils.toJsonPtr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import jakarta.annotation.Nonnull;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.rules.SetterArgs;

@Component
public class ReplaceProperty implements Transformer {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Pattern SUBSTITUTION_PATTERN =
            Pattern.compile("[\\w-]+(?<substitution>\\[% (?<placeHolder>\\w+) \\| (?<function>\\w+) %]).*");
    private static final String FUNCTION = "function";
    private static final String PLACE_HOLDER = "placeHolder";
    private static final String SUBSTITUTION = "substitution";

    @Override
    public void transform(JsonNode source,
            ObjectNode outputNode,
            String field,
            List<String> arguments,
            Map<String, String> contextValue) {

        String[] fields = field.split("\\."); // len = 2
        for (int i = 0; i < fields.length - 1; i++) {
            outputNode.putIfAbsent(fields[i], objectMapper.createObjectNode());
            outputNode = (ObjectNode) outputNode.at(toJsonPtr(fields[i]));
        }

        String finalField = fields[fields.length - 1];

        if (setterArgs.arguments().size() == 1) {
            outputNode.set(finalField, new TextNode(getReplacementValue(setterArgs, contextValues)));
        } else {
            ArrayNode leafNode = outputNode.putArray(finalField);
            setterArgs.arguments().forEach(leafNode::add);
        }
    }

    @Nonnull
    private static String getReplacementValue(SetterArgs setterArgs, Map<String, String> contextValues) {
        String replacementValue = setterArgs.arguments().getFirst();
        Matcher matcher = SUBSTITUTION_PATTERN.matcher(replacementValue);
        if (matcher.matches()) {
            if ("lc".equals(matcher.group(FUNCTION))) {
                String placeHolder = matcher.group(PLACE_HOLDER);
                replacementValue = replacementValue.replace(matcher.group(SUBSTITUTION),
                        contextValues.get(placeHolder).toLowerCase());
            } else {
                throw new IllegalArgumentException("Unexpected function type of %s".formatted(matcher.group(FUNCTION)));
            }
        }
        return replacementValue;
    }
}
