package uk.gov.companieshouse.filinghistory.consumer.mapper;

import com.fasterxml.jackson.databind.node.ObjectNode;

public record ChildPair(String childArrayKey, ObjectNode node) {

}
