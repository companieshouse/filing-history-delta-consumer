package uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform;

import static uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform.MapperUtils.getFieldValueFromJsonNode;
import static uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform.MapperUtils.getNestedJsonNodeFromJsonNode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.AltCapitalDescriptionValue;
import uk.gov.companieshouse.api.filinghistory.CapitalDescriptionValue;
import uk.gov.companieshouse.api.filinghistory.DescriptionValues;
import uk.gov.companieshouse.filinghistory.consumer.serdes.ArrayNodeDeserialiser;

@Component
public class DescriptionValuesMapper {

    private final ArrayNodeDeserialiser<AltCapitalDescriptionValue> altCapitalArrayNodeDeserialiser;
    private final ArrayNodeDeserialiser<CapitalDescriptionValue> capitalArrayNodeDeserialiser;
    private final JsonNodeCleaner jsonNodeCleaner;

    public DescriptionValuesMapper(ArrayNodeDeserialiser<AltCapitalDescriptionValue> altCapitalArrayNodeDeserialiser,
                                   ArrayNodeDeserialiser<CapitalDescriptionValue> capitalArrayNodeDeserialiser, JsonNodeCleaner jsonNodeCleaner) {
        this.altCapitalArrayNodeDeserialiser = altCapitalArrayNodeDeserialiser;
        this.capitalArrayNodeDeserialiser = capitalArrayNodeDeserialiser;
        this.jsonNodeCleaner = jsonNodeCleaner;
    }

    public DescriptionValues map(final JsonNode inputNode) {
        if (inputNode == null) {
            return null;
        }

        JsonNode jsonNode = jsonNodeCleaner.setEmptyStringsToNull(inputNode);

        List<AltCapitalDescriptionValue> altCapital = Optional.ofNullable(
                        getNestedJsonNodeFromJsonNode(jsonNode, "alt_capital"))
                .map(altCapitalArray -> altCapitalArrayNodeDeserialiser.deserialise((ArrayNode) altCapitalArray))
                .orElse(null);

        List<CapitalDescriptionValue> capital = Optional.ofNullable(
                        getNestedJsonNodeFromJsonNode(jsonNode, "capital"))
                .map(capitalArray -> capitalArrayNodeDeserialiser.deserialise((ArrayNode) capitalArray))
                .orElse(null);

        DescriptionValues descriptionValues = new DescriptionValues()
                .altCapital(altCapital)
                .appointmentDate(getFieldValueFromJsonNode(jsonNode, "appointment_date"))
                .branchNumber(getFieldValueFromJsonNode(jsonNode, "branch_number"))
                .broughtDownDate(getFieldValueFromJsonNode(jsonNode, "brought_down_date"))
                .capital(capital)
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

        return descriptionValues.equals(new DescriptionValues()) ? null : descriptionValues;
    }
}
