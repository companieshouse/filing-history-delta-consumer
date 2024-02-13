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
        final String barcode = filingHistory.getBarcode();
        if (StringUtils.isNotBlank(barcode)) {
            objectNode.put("_barcode", barcode);
        }

        final String documentId = filingHistory.getDocumentId();
        if (StringUtils.isNotBlank(documentId)) {
            objectNode.put("_document_id", documentId);
        }
    }

    private void mapDescriptionValuesObject(ObjectNode objectNode, final DescriptionValues descriptionValues) {
        if (descriptionValues == null) {
            return;
        }

        final String resignationDate = descriptionValues.getResignationDate();
        final String officerName = descriptionValues.getOFFICERNAME();

        if (StringUtils.isNotBlank(resignationDate) && StringUtils.isNotBlank(officerName)) {
            objectNode
                    .putObject("original_values")
                    .put("resignation_date", resignationDate)
                    .put("officer_name", officerName);
        }
    }

    private void mapDataObject(ObjectNode objectNode, final FilingHistory filingHistory) {
        objectNode
                .putObject("data")
                .put("type", filingHistory.getFormType())
                .put("date", formatDate.format(filingHistory.getReceiveDate()))
                .put("description", filingHistory.getDescription())
                .put("category", filingHistory.getCategory());
    }
}