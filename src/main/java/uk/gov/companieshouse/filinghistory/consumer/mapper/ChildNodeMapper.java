package uk.gov.companieshouse.filinghistory.consumer.mapper;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import uk.gov.companieshouse.api.delta.FilingHistory;

public interface ChildNodeMapper {

    Map<String, ObjectNode> mapChildObjectNode(FilingHistory delta);
}
