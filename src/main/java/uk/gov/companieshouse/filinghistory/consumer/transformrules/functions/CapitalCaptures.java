package uk.gov.companieshouse.filinghistory.consumer.transformrules.functions;

import tools.jackson.databind.node.ArrayNode;

record CapitalCaptures(ArrayNode captures, ArrayNode altCaptures) {

}