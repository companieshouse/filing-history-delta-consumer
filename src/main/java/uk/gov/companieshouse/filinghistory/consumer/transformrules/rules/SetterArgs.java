package uk.gov.companieshouse.filinghistory.consumer.transformrules.rules;

import java.util.List;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.Transformer;

public record SetterArgs(Transformer transformer, List<String> arguments) {

}
