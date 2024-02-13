package uk.gov.companieshouse.filinghistory.consumer.transformrules.functions;

import com.fasterxml.jackson.databind.node.ObjectNode;

public record TransformTarget(String fieldKey, String fieldValue, ObjectNode objectNode) {

}
