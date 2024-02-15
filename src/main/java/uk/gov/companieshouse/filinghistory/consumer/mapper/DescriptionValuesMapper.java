package uk.gov.companieshouse.filinghistory.consumer.mapper;

import static uk.gov.companieshouse.filinghistory.consumer.mapper.MapperUtils.getFieldValueFromJsonNode;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.FilingHistoryItemDataDescriptionValues;

@Component
public class DescriptionValuesMapper {

    public FilingHistoryItemDataDescriptionValues map(final JsonNode jsonNode) {
        if (jsonNode == null) {
            return null;
        }
        return new FilingHistoryItemDataDescriptionValues()
                .appointmentDate(getFieldValueFromJsonNode(jsonNode, "appointment_date"))
                .branchNumber(getFieldValueFromJsonNode(jsonNode, "branch_number"))
                .broughtDownDate(getFieldValueFromJsonNode(jsonNode, "brought_down_date"))
                .caseEndDate(getFieldValueFromJsonNode(jsonNode, "case_end_date"))
                .caseNumber(getFieldValueFromJsonNode(jsonNode, "case_number"))
                .cessationDate(getFieldValueFromJsonNode(jsonNode, "cessation_date"))
                .changeAddress(getFieldValueFromJsonNode(jsonNode, "change_address"))
                .changeDate(getFieldValueFromJsonNode(jsonNode, "change_date"))
                .changeDetails(getFieldValueFromJsonNode(jsonNode, "change_details"))
                .changeName(getFieldValueFromJsonNode(jsonNode, "change_name"))
                .changeType(getFieldValueFromJsonNode(jsonNode, "change_type"))
                .chargeCreationDate(getFieldValueFromJsonNode(jsonNode, "charge_creation_date"))
                .chargeNumber(getFieldValueFromJsonNode(jsonNode, "charge_number"))
                .closeDate(getFieldValueFromJsonNode(jsonNode, "close_date"))
                .companyNumber(getFieldValueFromJsonNode(jsonNode, "company_number"))
                .companyType(getFieldValueFromJsonNode(jsonNode, "company_type"))
                .date(getFieldValueFromJsonNode(jsonNode, "date"))
                .defaultAddress(getFieldValueFromJsonNode(jsonNode, "default_address"))
                .description(getFieldValueFromJsonNode(jsonNode, "description"))
                .formAttached(getFieldValueFromJsonNode(jsonNode, "form_attached"))
                .formType(getFieldValueFromJsonNode(jsonNode, "form_type"))
                .incorporationDate(getFieldValueFromJsonNode(jsonNode, "incorporation_date"))
                .madeUpDate(getFieldValueFromJsonNode(jsonNode, "made_up_date"))
                .newAddress(getFieldValueFromJsonNode(jsonNode, "new_address"))
                .newDate(getFieldValueFromJsonNode(jsonNode, "new_date"))
                .newJurisdiction(getFieldValueFromJsonNode(jsonNode, "new_jurisdiction"))
                .notificationDate(getFieldValueFromJsonNode(jsonNode, "notification_date"))
                .officerAddress(getFieldValueFromJsonNode(jsonNode, "officer_address"))
                .officerName(getFieldValueFromJsonNode(jsonNode, "officer_name"))
                .oldAddress(getFieldValueFromJsonNode(jsonNode, "old_address"))
                .oldJurisdiction(getFieldValueFromJsonNode(jsonNode, "old_jurisdiction"))
                .originalDescription(getFieldValueFromJsonNode(jsonNode, "original_description"))
                .propertyAcquiredDate(getFieldValueFromJsonNode(jsonNode, "property_acquired_date"))
                .pscName(getFieldValueFromJsonNode(jsonNode, "psc_name"))
                .representativeDetails(getFieldValueFromJsonNode(jsonNode, "representative_details"))
                .terminationDate(getFieldValueFromJsonNode(jsonNode, "termination_date"))
                .withdrawalDate(getFieldValueFromJsonNode(jsonNode, "withdrawal_date"));
    }
}
