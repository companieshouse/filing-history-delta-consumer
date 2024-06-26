package uk.gov.companieshouse.filinghistory.consumer.mapper.pretransform;

import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.companieshouse.api.delta.FilingHistory;

public interface ChildNodeMapper {

    ObjectNode mapChildObjectNode(FilingHistory filingHistory, ObjectNode parentNode);
}
