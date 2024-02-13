package uk.gov.companieshouse.filinghistory.consumer.transformrules.functions;

import com.fasterxml.jackson.databind.node.ArrayNode;

record CapitalCaptures(ArrayNode captures, ArrayNode altCaptures) {

}