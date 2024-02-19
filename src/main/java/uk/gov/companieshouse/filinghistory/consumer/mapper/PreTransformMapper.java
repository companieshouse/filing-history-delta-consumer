package uk.gov.companieshouse.filinghistory.consumer.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.DescriptionValues;
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.FormatDate;

@Component
public class PreTransformMapper {

    private final ObjectMapper objectMapper;
    private final FormatDate formatDate;

    public PreTransformMapper(ObjectMapper objectMapper, FormatDate formatDate) {
        this.objectMapper = objectMapper;
        this.formatDate = formatDate;
    }

    public ObjectNode mapDeltaToObjectNode(final FilingHistory filingHistory) {
        final ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode
                .put("company_number", filingHistory.getCompanyNumber())
                .put("_entity_id", filingHistory.getEntityId())
                .put("parent_entity_id", filingHistory.getParentEntityId())
                .put("parent_form_type", filingHistory.getParentFormType())
                .put("pre_scanned_batch", filingHistory.getPreScannedBatch());

        mapBarcodeAndDocumentId(objectNode, filingHistory);
        mapDescriptionValuesObject(objectNode, filingHistory.getDescriptionValues());
        mapDataObject(objectNode, filingHistory);

        return objectNode;
    }

    private void mapBarcodeAndDocumentId(ObjectNode objectNode, final FilingHistory filingHistory) {
        putIfNotBlank(objectNode, "_barcode", filingHistory.getBarcode());
        putIfNotBlank(objectNode, "_document_id", filingHistory.getDocumentId());
    }

    private void mapDescriptionValuesObject(ObjectNode objectNode, final DescriptionValues descriptionValues) {
        if (descriptionValues == null) {
            return;
        }
        ObjectNode originalValuesNode = objectNode.putObject("original_values");

        putIfNotBlank(originalValuesNode, "resignation_date", descriptionValues.getResignationDate());
        putIfNotBlank(originalValuesNode, "officer_name", descriptionValues.getOFFICERNAME());
        putIfNotBlank(originalValuesNode, "officer_name", descriptionValues.getOfficerName());
        putIfNotBlank(originalValuesNode, "cessation_date", descriptionValues.getCessationDate());
        putIfNotBlank(originalValuesNode, "change_date", descriptionValues.getChangeDate());
        putIfNotBlank(originalValuesNode, "notification_date", descriptionValues.getNotificationDate());
        putIfNotBlank(originalValuesNode, "psc_name", descriptionValues.getPscName());
        putIfNotBlank(originalValuesNode, "acc_type", descriptionValues.getAccType());
        putIfNotBlank(originalValuesNode, "case_end_date", descriptionValues.getCaseEndDate());
        putIfNotBlank(originalValuesNode, "made_up_date", descriptionValues.getMadeUpDate());
        putIfNotBlank(originalValuesNode, "new_ro_address", descriptionValues.getNewRoAddress());
        putIfNotBlank(originalValuesNode, "appointment_date", descriptionValues.getAppointmentDate());
        putIfNotBlank(originalValuesNode, "charge_creation_date", descriptionValues.getChargeCreationDate());
        putIfNotBlank(originalValuesNode, "property_acquired_date", descriptionValues.getPropertyAcquiredDate());
        putIfNotBlank(originalValuesNode, "notification_date", descriptionValues.getNotificationDate());
        putIfNotBlank(originalValuesNode, "accounting_period", descriptionValues.getAccountingPeriod());
        putIfNotBlank(originalValuesNode, "period_type", descriptionValues.getPeriodType());
        putIfNotBlank(originalValuesNode, "new_date", descriptionValues.getNewDate());
    }

    private void mapDataObject(ObjectNode objectNode, final FilingHistory filingHistory) {
        objectNode
                .putObject("data")
                .put("type", filingHistory.getFormType())
                .put("date", formatDate.format(filingHistory.getReceiveDate()))
                .put("description", filingHistory.getDescription())
                .put("category", filingHistory.getCategory());
    }

    private static void putIfNotBlank(ObjectNode node, String key, String value) {
        if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
            node.put(key, value);
        }
    }
}