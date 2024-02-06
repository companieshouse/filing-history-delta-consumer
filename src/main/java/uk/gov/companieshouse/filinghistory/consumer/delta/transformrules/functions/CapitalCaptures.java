package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import com.fasterxml.jackson.databind.node.ArrayNode;

record CapitalCaptures(ArrayNode captures, ArrayNode altCaptures) {

}