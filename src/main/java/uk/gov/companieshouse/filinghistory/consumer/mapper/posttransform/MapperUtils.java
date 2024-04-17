package uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform;

import com.fasterxml.jackson.databind.JsonNode;

public final class MapperUtils {

    private MapperUtils() {
    }

    static String getFieldValueFromJsonNode(final JsonNode node, final String field) {
        if (node == null) {
            return null;
        }
        return node.get(field) != null ? node.get(field).textValue() : null;
    }

    static JsonNode getNestedJsonNodeFromJsonNode(final JsonNode node, final String field) {
        if (node == null) {
            return null;
        }
        return node.get(field) != null ? node.get(field) : null;
    }
}
