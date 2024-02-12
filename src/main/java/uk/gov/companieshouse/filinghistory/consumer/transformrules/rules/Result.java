package uk.gov.companieshouse.filinghistory.consumer.transformrules.rules;

import java.util.Map;

public record Result(boolean matched, Map<String, String> contextData) {
        
}