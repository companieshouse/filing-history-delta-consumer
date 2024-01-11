package uk.gov.companieshouse.filinghistory.consumer.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;

public class FormatProperty implements Transformer {

    @Override
    public void transform(JsonNode inputRequest, ObjectNode outputRequest, String field, SetterArgs arguments,
            Map<String, String> contextValue) {

    }
}
