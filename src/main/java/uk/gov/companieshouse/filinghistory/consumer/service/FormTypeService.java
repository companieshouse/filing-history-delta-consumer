package uk.gov.companieshouse.filinghistory.consumer.service;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class FormTypeService {

    private static final String ANNOTATION_FORM_TYPE = "ANNOTATION";
    private static final String ASSOCIATED_FILING_FORM_TYPE = "ASSOCIATED-FILING";
    private static final String TOP_LEVEL_FORM_TYPE = "TOP-LEVEL";
    private final List<String> formTypeBlacklist;

    public FormTypeService(List<String> formTypeBlacklist) {
        this.formTypeBlacklist = formTypeBlacklist;
    }

    public String getFormType(TransactionKindCriteria transactionKindCriteria) {
        final String deltaParentFormType = transactionKindCriteria.parentFormType();
        final String deltaFormType = transactionKindCriteria.formType();
        final String parentEntityId = transactionKindCriteria.parentEntityId();

        final String formType;

        if (ANNOTATION_FORM_TYPE.equals(deltaParentFormType)) {
            formType = ANNOTATION_FORM_TYPE;
        } else if (isAssociatedFiling(deltaParentFormType, deltaFormType, parentEntityId)) {
            formType = ASSOCIATED_FILING_FORM_TYPE;
        } else {
            formType = TOP_LEVEL_FORM_TYPE;
        }
        return formType;
    }

    private boolean isAssociatedFiling(final String deltaParentFormType,
                                       final String deltaFormType,
                                       final String parentEntityId) {
        final boolean isAssociatedFiling;

        // TODO: Check for null/empty form types?

        if (formTypeBlacklist.contains(deltaParentFormType) || formTypeBlacklist.contains(deltaFormType)) {
            isAssociatedFiling = false;
        } else isAssociatedFiling = StringUtils.isNotBlank(parentEntityId);
        return isAssociatedFiling;
    }
}
