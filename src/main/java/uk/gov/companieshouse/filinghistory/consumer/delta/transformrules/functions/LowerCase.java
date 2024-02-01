package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class LowerCase extends AbstractTransformer {

    public LowerCase(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    protected void doTransform(JsonNode source, TransformTarget target, List<String> arguments,
            Map<String, String> context) {
        target.objectNode().put(target.field(), transformLowerCase(arguments.getFirst()));
    }

    String transformLowerCase(String nodeText) {
        if (StringUtils.isBlank(nodeText)) {
            return nodeText;
        }
        return nodeText.toLowerCase();
    }
}
