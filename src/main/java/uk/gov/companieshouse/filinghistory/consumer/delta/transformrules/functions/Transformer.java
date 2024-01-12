package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.rules.SetterArgs;

@FunctionalInterface
public interface Transformer {

    void transform(ObjectNode request,
            String field,
            SetterArgs arguments,
            Map<String, String> contextValue);
}
