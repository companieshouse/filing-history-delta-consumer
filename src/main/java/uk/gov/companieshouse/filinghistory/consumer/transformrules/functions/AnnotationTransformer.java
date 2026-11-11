package uk.gov.companieshouse.filinghistory.consumer.transformrules.functions;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

@Component
public class AnnotationTransformer implements Transformer {

    @Override
    public void transform(JsonNode source, ObjectNode outputNode, String field, List<String> arguments,
                          Map<String, String> contextValue) {
        String[] fields = field.split("\\.");
        String finalField = fields[fields.length - 1];

        outputNode.put(finalField, arguments.getFirst());
    }
}
