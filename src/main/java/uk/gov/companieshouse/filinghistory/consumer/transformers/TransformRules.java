package uk.gov.companieshouse.filinghistory.consumer.transformers;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TransformRules {

    private final List<TransformRule> compiledRules;

    public TransformRules(List<TransformRule> compiledRules) {
        this.compiledRules = compiledRules;
    }
}
