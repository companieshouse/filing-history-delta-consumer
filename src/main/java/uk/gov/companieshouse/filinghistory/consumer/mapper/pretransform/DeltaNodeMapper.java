package uk.gov.companieshouse.filinghistory.consumer.mapper.pretransform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.DescriptionValues;
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.FormatDate;

@Component
public class DeltaNodeMapper extends AbstractNodeMapper implements NodeMapper {

    private final CategoryMapper categoryMapper;

    protected DeltaNodeMapper(ObjectMapper objectMapper, FormatDate formatDate, CategoryMapper categoryMapper) {
        super(objectMapper, formatDate);
        this.categoryMapper = categoryMapper;
    }

    @Override
    public ObjectNode mapToObjectNode(FilingHistory filingHistory) {
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

        putIfNotBlank(originalValuesNode, "acc_type", descriptionValues.getAccType());
        putIfNotBlank(originalValuesNode, "accounting_period", descriptionValues.getAccountingPeriod());
        putIfNotBlank(originalValuesNode, "action", descriptionValues.getAction());
        putIfNotBlank(originalValuesNode, "appointment_date", descriptionValues.getAppointmentDate());
        putIfNotBlank(originalValuesNode, "capital_type", descriptionValues.getCapitalType());
        putIfNotBlank(originalValuesNode, "case_start_date", descriptionValues.getCaseStartDate());
        putIfNotBlank(originalValuesNode, "case_end_date", descriptionValues.getCaseEndDate());
        putIfNotBlank(originalValuesNode, "cessation_date", descriptionValues.getCessationDate());
        putIfNotBlank(originalValuesNode, "change_date", descriptionValues.getChangeDate());
        putIfNotBlank(originalValuesNode, "charge_creation_date", descriptionValues.getChargeCreationDate());
        putIfNotBlank(originalValuesNode, "made_up_date", descriptionValues.getMadeUpDate());
        putIfNotBlank(originalValuesNode, "mortgage_satisfaction_date",
                descriptionValues.getMortgageSatisfactionDate());
        putIfNotBlank(originalValuesNode, "new_ro_address", descriptionValues.getNewRoAddress());
        putIfNotBlank(originalValuesNode, "new_date", descriptionValues.getNewDate());
        putIfNotBlank(originalValuesNode, "notification_date", descriptionValues.getNotificationDate());
        putIfNotBlank(originalValuesNode, "officer_name", descriptionValues.getOFFICERNAME());
        putIfNotBlank(originalValuesNode, "officer_name", descriptionValues.getOfficerName());
        putIfNotBlank(originalValuesNode, "period_type", descriptionValues.getPeriodType());
        putIfNotBlank(originalValuesNode, "property_acquired_date", descriptionValues.getPropertyAcquiredDate());
        putIfNotBlank(originalValuesNode, "psc_name", descriptionValues.getPscName());
        putIfNotBlank(originalValuesNode, "resignation_date", descriptionValues.getResignationDate());
    }

    private void mapDataObject(ObjectNode objectNode, final FilingHistory filingHistory) {
        objectNode
                .putObject("data")
                .put("type", filingHistory.getFormType())
                .put("date", formatDate.format(filingHistory.getReceiveDate()))
                .put("description", filingHistory.getDescription()
                        .replace("<", "\\")
                        .replace("\n", "\\"))
                .put("category", categoryMapper.map(filingHistory.getCategory()));
    }
}
