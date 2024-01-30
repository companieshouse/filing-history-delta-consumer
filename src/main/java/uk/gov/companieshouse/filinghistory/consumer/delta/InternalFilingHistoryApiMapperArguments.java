package uk.gov.companieshouse.filinghistory.consumer.delta;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;

public record InternalFilingHistoryApiMapperArguments(JsonNode topLevelNode, TransactionKindResult kindResult, String companyNumber, String deltaAt, String updatedBy) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InternalFilingHistoryApiMapperArguments arguments = (InternalFilingHistoryApiMapperArguments) o;
        return Objects.equals(topLevelNode, arguments.topLevelNode) && Objects.equals(kindResult, arguments.kindResult) && Objects.equals(companyNumber, arguments.companyNumber) && Objects.equals(deltaAt, arguments.deltaAt) && Objects.equals(updatedBy, arguments.updatedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topLevelNode, kindResult, companyNumber, deltaAt, updatedBy);
    }
}
