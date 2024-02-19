package uk.gov.companieshouse.filinghistory.consumer.transformrules.functions;

import static uk.gov.companieshouse.filinghistory.consumer.transformrules.TransformerUtils.toJsonPtr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class ProcessCapital {

    private final ObjectMapper objectMapper;
    private final CapitalCaptor capitalCaptor;

    public ProcessCapital(ObjectMapper objectMapper, CapitalCaptor capitalCaptor) {
        this.objectMapper = objectMapper;
        this.capitalCaptor = capitalCaptor;
    }

    public void transform(JsonNode source, ObjectNode outputNode,
            String fieldPath, Pattern extract, String altDescription) {

        String[] fields = fieldPath.split("\\.");
        for (int i = 0; i < fields.length - 1; i++) {
            outputNode.putIfAbsent(fields[i], objectMapper.createObjectNode());
            outputNode = (ObjectNode) outputNode.at("/" + fields[i]);
        }

        String sourceDescription = source.at(toJsonPtr(fieldPath)).textValue();

        CapitalCaptures capitalCaptures = capitalCaptor.captureCapital(extract, altDescription, sourceDescription);

        outputNode.putIfAbsent("description_values", objectMapper.createObjectNode());
        ObjectNode descriptionValues = (ObjectNode) outputNode.at(toJsonPtr("description_values"));

        descriptionValues
                .putArray("capital")
                .addAll(capitalCaptures.captures());

        if (!capitalCaptures.altCaptures().isEmpty()) {
            descriptionValues.
                    putArray("alt_capital")
                    .addAll(capitalCaptures.altCaptures());
        }
    }
}
