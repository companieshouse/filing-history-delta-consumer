package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import com.fasterxml.jackson.databind.node.ObjectNode;

public record TransformTarget(String field, ObjectNode objectNode) {

}
