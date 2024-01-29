package uk.gov.companieshouse.filinghistory.consumer.delta;

import com.fasterxml.jackson.databind.JsonNode;

public final class MappingUtils {

    private MappingUtils() {}

    static String getValueFromField(final JsonNode node, final String field) {
        return node.get(field) != null ? node.get(field).textValue() : null;
    }
}
