package uk.gov.companieshouse.filinghistory.consumer.mapper;

import static uk.gov.companieshouse.filinghistory.consumer.mapper.MapperUtils.getFieldValueFromJsonNode;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalDataOriginalValues;

@Component
public class OriginalValuesMapper {

    public InternalDataOriginalValues map(JsonNode jsonNode) {
        if (jsonNode == null) {
            return null;
        }
        return new InternalDataOriginalValues()
                .accType(getFieldValueFromJsonNode(jsonNode, "acc_type"))
                .accountingPeriod(getFieldValueFromJsonNode(jsonNode, "accounting_period"))
                .action(getFieldValueFromJsonNode(jsonNode, "action"))
                .appointmentDate(getFieldValueFromJsonNode(jsonNode, "appointment_date"))
                .capitalType(getFieldValueFromJsonNode(jsonNode, "capital_type"))
                .caseStartDate(getFieldValueFromJsonNode(jsonNode, "case_start_date"))
                .caseEndDate(getFieldValueFromJsonNode(jsonNode, "case_end_date"))
                .cessationDate(getFieldValueFromJsonNode(jsonNode, "cessation_date"))
                .changeDate(getFieldValueFromJsonNode(jsonNode, "change_date"))
                .chargeCreationDate(getFieldValueFromJsonNode(jsonNode, "charge_creation_date"))
                .madeUpDate(getFieldValueFromJsonNode(jsonNode, "made_up_date"))
                .mortgageSatisfactionDate(getFieldValueFromJsonNode(jsonNode, "mortgage_satisfaction_date"))
                .newRoAddress(getFieldValueFromJsonNode(jsonNode, "new_ro_address"))
                .newDate(getFieldValueFromJsonNode(jsonNode, "new_date"))
                .notificationDate(getFieldValueFromJsonNode(jsonNode, "notification_date"))
                .officerName(getFieldValueFromJsonNode(jsonNode, "officer_name"))
                .periodType(getFieldValueFromJsonNode(jsonNode, "period_type"))
                .propertyAcquiredDate(getFieldValueFromJsonNode(jsonNode, "property_acquired_date"))
                .pscName(getFieldValueFromJsonNode(jsonNode, "psc_name"))
                .resignationDate(getFieldValueFromJsonNode(jsonNode, "resignation_date"));
    }
}
