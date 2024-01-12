package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.rules;

import java.util.List;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.Transformer;

public record SetterArgs(Transformer transformer, List<String> arguments) {

}
