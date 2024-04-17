package uk.gov.companieshouse.filinghistory.consumer.mapper.pretransform;

import com.fasterxml.jackson.databind.node.ObjectNode;

public record ChildPair(String childArrayKey, ObjectNode node) {

}
