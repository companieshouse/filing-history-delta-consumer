package uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.filinghistory.consumer.serdes.ArrayNodeDeserialiser;

@Component
public class SubcategoryMapper {

    private final ArrayNodeDeserialiser<String> stringArrayNodeDeserialiser;

    public SubcategoryMapper(ArrayNodeDeserialiser<String> stringArrayNodeDeserialiser) {
        this.stringArrayNodeDeserialiser = stringArrayNodeDeserialiser;
    }

    public Object map(JsonNode data) {
        if (data == null) {
            return null;
        }
        JsonNode subcategoryNode = data.get("subcategory");

        return switch (subcategoryNode) {
            case null -> null;
            case ArrayNode arrayNode -> stringArrayNodeDeserialiser.deserialise(arrayNode);
            default -> subcategoryNode.textValue();
        };
    }
}
