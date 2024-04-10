package uk.gov.companieshouse.filinghistory.consumer.service;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FormTypeService {

    private final List<String> formTypeBlacklist;

    public FormTypeService(List<String> formTypeBlacklist) {
        this.formTypeBlacklist = formTypeBlacklist;
    }

    public boolean isAssociatedFilingBlacklisted(TransactionKindCriteria criteria) {
        return formTypeBlacklist.contains(criteria.parentFormType()) || formTypeBlacklist.contains(criteria.formType());
    }
}
