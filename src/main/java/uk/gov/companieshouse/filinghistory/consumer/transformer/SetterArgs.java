package uk.gov.companieshouse.filinghistory.consumer.transformer;

import java.util.List;

public record SetterArgs(Transformer transformer, List<String> arguments) {

}
