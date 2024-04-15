package uk.gov.companieshouse.filinghistory.consumer.transformrules.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AnnotationTransformer implements Transformer {

    @Override
    public void transform(JsonNode source, ObjectNode outputNode, String field, List<String> arguments,
                          Map<String, String> contextValue) {

    }
}
