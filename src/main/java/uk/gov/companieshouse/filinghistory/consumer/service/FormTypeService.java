package uk.gov.companieshouse.filinghistory.consumer.service;

import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class FormTypeService {

    private static final Pattern RESOLUTION_PATTERN = Pattern.compile(
            "^(?:E|(?:\\(W\\))*EL|S|L|W|O|)(?!<AU|CN)RES(?!T)");
    private final List<String> formTypeBlacklist;

    public FormTypeService(List<String> formTypeBlacklist) {
        this.formTypeBlacklist = formTypeBlacklist;
    }

    public boolean isAssociatedFilingBlacklisted(TransactionKindCriteria criteria) {
        return formTypeBlacklist.contains(criteria.parentFormType()) || formTypeBlacklist.contains(criteria.formType());
    }

    public boolean isResolutionType(String formType) {
        return StringUtils.isNotBlank(formType) && RESOLUTION_PATTERN.matcher(formType).find();
    }
}
