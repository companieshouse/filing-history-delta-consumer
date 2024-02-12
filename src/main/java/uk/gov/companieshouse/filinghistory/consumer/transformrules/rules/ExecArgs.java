package uk.gov.companieshouse.filinghistory.consumer.transformrules.rules;

import java.util.regex.Pattern;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.ProcessCapital;

public record ExecArgs(ProcessCapital processCapital, String fieldPath, Pattern extract,
                       String altDescription) {

}
