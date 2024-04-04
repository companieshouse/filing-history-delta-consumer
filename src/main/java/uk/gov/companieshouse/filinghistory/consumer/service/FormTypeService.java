package uk.gov.companieshouse.filinghistory.consumer.service;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class FormTypeService {

    private final List<String> formTypeBlacklist;

    public FormTypeService(List<String> formTypeBlacklist) {
        this.formTypeBlacklist = formTypeBlacklist;
    }

    public boolean isAssociatedFiling(TransactionKindCriteria criteria) {
        boolean isBlacklisted = formTypeBlacklist.contains(criteria.parentFormType()) ||
                formTypeBlacklist.contains(criteria.formType());
        return !isBlacklisted && StringUtils.isNotBlank(criteria.parentEntityId());
    }
}
