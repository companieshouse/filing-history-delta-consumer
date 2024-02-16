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
                .accountingPeriod(getFieldValueFromJsonNode(jsonNode, "accounting_period")) // in Mongo and spec but not on YAML
//                .action(getFieldValueFromJsonNode(jsonNode, "action")) // in Mongo but not on spec or YAML
                .appointmentDate(getFieldValueFromJsonNode(jsonNode, "appointment_date"))
//                .capitalType(getFieldValueFromJsonNode(jsonNode, "capital_type")) // in Mongo but not on spec or YAML
                .caseStartDate(getFieldValueFromJsonNode(jsonNode, "case_start_date")) // in Mongo and spec but not on YAML
                .caseEndDate(getFieldValueFromJsonNode(jsonNode, "case_end_date"))
                .cessationDate(getFieldValueFromJsonNode(jsonNode, "cessation_date"))
                .changeDate(getFieldValueFromJsonNode(jsonNode, "change_date"))
                .chargeCreationDate(getFieldValueFromJsonNode(jsonNode, "charge_creation_date"))
                .madeUpDate(getFieldValueFromJsonNode(jsonNode, "made_up_date"))
//                .mortgageSatisfactionDate(getFieldValueFromJsonNode(jsonNode, "mortgage_satisfaction_date")) // in Mongo but not on spec or YAML
                .newRoAddress(getFieldValueFromJsonNode(jsonNode, "new_ro_address"))
                .newDate(getFieldValueFromJsonNode(jsonNode, "new_date")) // in Mongo and on spec but not on YAML
                .notificationDate(getFieldValueFromJsonNode(jsonNode, "notification_date"))
                .officerName(getFieldValueFromJsonNode(jsonNode, "officer_name"))
                .periodType(getFieldValueFromJsonNode(jsonNode, "period_type")) // in Mongo and spec but not on YAML
                .propertyAcquiredDate(getFieldValueFromJsonNode(jsonNode, "property_acquired_date"))
                .pscName(getFieldValueFromJsonNode(jsonNode, "psc_name"))
                .resignationDate(getFieldValueFromJsonNode(jsonNode, "resignation_date"));
    }
}
