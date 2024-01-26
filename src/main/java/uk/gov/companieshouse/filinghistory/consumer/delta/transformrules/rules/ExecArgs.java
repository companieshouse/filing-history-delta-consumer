package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.rules;

import java.util.regex.Pattern;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.ProcessCapital;

public record ExecArgs(ProcessCapital processCapital, String fieldPath, Pattern extract,
                       String altDescription) {

}
