package uk.gov.companieshouse.filinghistory.consumer.mapper;

import uk.gov.companieshouse.api.delta.FilingHistory;

public interface ChildNodeMapper {

    ChildPair mapChildObjectNode(FilingHistory delta);
}
