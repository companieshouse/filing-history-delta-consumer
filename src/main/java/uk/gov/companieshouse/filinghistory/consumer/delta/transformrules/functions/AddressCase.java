package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class AddressCase extends AbstractTransformer {

    private static final Pattern POST_CODE_PATTERN = Pattern.compile(
            "(\\b[A-Z][A-Z]?\\d[A-Z\\d]?\\s*\\d[A-Z]{2}\\b)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PO_BOX_PATTERN = Pattern.compile("\\bPo\\s+Box\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern NUMBER_SUFFIX_PATTERN = Pattern.compile("(\\b\\d+\\s*(nd|th|rd|st)\\b)",
            Pattern.CASE_INSENSITIVE);

    private final TitleCase titleCase;

    public AddressCase(ObjectMapper objectMapper, TitleCase titleCase) {
        super(objectMapper);
        this.titleCase = titleCase;
    }

    @Override
    protected void doTransform(JsonNode source, TransformTarget target, List<String> arguments,
            Map<String, String> context) {
        String targetValue = getFieldToTransform(source, arguments, context);
        target.objectNode().put(target.fieldKey(), transformAddressCase(targetValue));
    }

    String transformAddressCase(String nodeText) {
        if (StringUtils.isEmpty(nodeText)) {
            return nodeText;
        }
        nodeText = titleCase.transformTitleCase(nodeText);
        nodeText = mapToken(POST_CODE_PATTERN, nodeText, (word, matcher)
                -> matcher.group(1).toUpperCase(Locale.UK), true);
        nodeText = PO_BOX_PATTERN.matcher(nodeText).replaceFirst("PO Box");
        nodeText = mapToken(NUMBER_SUFFIX_PATTERN, nodeText, (word, matcher)
                -> matcher.group(1).toLowerCase(Locale.UK), true);
        return nodeText.trim();
    }
}
