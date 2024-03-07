package uk.gov.companieshouse.filinghistory.consumer.transformrules.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class ReplaceProperty extends AbstractTransformer {

    private static final Pattern SUBSTITUTION_PATTERN =
            Pattern.compile("[\\w-]+(?<substitution>\\[% (?<placeHolder>\\w+) \\| (?<function>\\w+) %]).*");
    private static final String FUNCTION = "function";
    private static final String PLACE_HOLDER = "placeHolder";
    private static final String SUBSTITUTION = "substitution";

    private final LowerCase lowerCase;

    public ReplaceProperty(ObjectMapper objectMapper, LowerCase lowerCase) {
        super(objectMapper);
        this.lowerCase = lowerCase;
    }

    @Override
    protected void doTransform(JsonNode source, TransformTarget target, List<String> arguments,
            Map<String, String> context) {
        if (arguments.size() == 1) {
            target.objectNode().put(target.fieldKey(), getReplacementValue(target.fieldValue(), context));
        } else {
            ArrayNode leafNode = target.objectNode().putArray(target.fieldKey());
            arguments.forEach(leafNode::add);
        }
    }

    private String getReplacementValue(String fieldValue, Map<String, String> context) {
        Matcher matcher = SUBSTITUTION_PATTERN.matcher(fieldValue);
        if (matcher.matches()) {
            if ("lc".equals(matcher.group(FUNCTION))) {
                String placeHolder = matcher.group(PLACE_HOLDER);
                fieldValue = fieldValue.replace(matcher.group(SUBSTITUTION),
                        lowerCase.transformLowerCase(context.get(placeHolder)));
            } else {
                throw new IllegalArgumentException("Unexpected function type of %s".formatted(matcher.group(FUNCTION)));
            }
        }
        return fieldValue;
    }
}
