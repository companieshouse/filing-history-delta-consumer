package uk.gov.companieshouse.filinghistory.consumer.delta;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.DescriptionValues;
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.api.delta.FilingHistoryDelta;

@Component
public class PreTransformMapper {

    private final ObjectMapper objectMapper;

    public PreTransformMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ObjectNode mapDeltaToObjectNode(final FilingHistoryDelta delta) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        final FilingHistory filingHistory = delta.getFilingHistory().getFirst();

        objectNode.put("company_number", filingHistory.getCompanyNumber());
        objectNode.put("_entity_id", filingHistory.getEntityId());

        mapBarcodeAndDocumentId(objectNode, filingHistory);
        mapDescriptionValuesObject(objectNode, filingHistory.getDescriptionValues());
        mapDataObject(objectNode, filingHistory);

        return objectNode;
    }

    private void mapBarcodeAndDocumentId(ObjectNode objectNode, final FilingHistory filingHistory) {
        final String barcode = filingHistory.getBarcode();
        if (!StringUtils.isBlank(barcode)) {
            objectNode.put("_barcode", barcode);
        }

        final String documentId = filingHistory.getDocumentId();
        if (!StringUtils.isBlank(documentId)) {
            objectNode.put("_document_id", documentId);
        }
    }

    private void mapDescriptionValuesObject(ObjectNode objectNode, final DescriptionValues descriptionValues) {
        if (descriptionValues == null) {
            return;
        }

        final String resignationDate = descriptionValues.getResignationDate();
        final String officerName = descriptionValues.getOFFICERNAME();

        if (!StringUtils.isBlank(resignationDate) && !StringUtils.isBlank(officerName)) {
            objectNode.putObject("original_values")
                    .put("resignation_date", resignationDate)
                    .put("officer_name", officerName);
        }
    }

    private void mapDataObject(ObjectNode objectNode, final FilingHistory filingHistory) {
        objectNode.putObject("data")
                .put("type", filingHistory.getFormType())
                .put("date", filingHistory.getReceiveDate())
                .put("description", filingHistory.getDescription())
                .put("category", filingHistory.getCategory());
    }
}
