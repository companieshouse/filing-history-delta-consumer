package uk.gov.companieshouse.filinghistory.consumer.transformer;

import java.util.Map;

public record Result(boolean matched, Map<String, String> groups){
        
}