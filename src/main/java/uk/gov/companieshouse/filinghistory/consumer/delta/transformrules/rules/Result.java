package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.rules;

import java.util.Map;

public record Result(boolean matched, Map<String, String> contextData) {
        
}