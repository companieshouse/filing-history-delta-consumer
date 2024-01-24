package uk.gov.companieshouse.filinghistory.consumer.delta;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.DescriptionValues;
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.api.delta.FilingHistoryDelta;

@Component
public class PreTransformMapper {

    public Map<String, Object> map(final FilingHistoryDelta delta) {
        Map<String, Object> map = new HashMap<>();
        final FilingHistory filingHistory = delta.getFilingHistory().getFirst();

        map.put("company_number", filingHistory.getCompanyNumber());
        map.put("_entity_id", filingHistory.getEntityId());

        mapBarcodeAndDocumentId(map, filingHistory);
        mapDescriptionValues(map, filingHistory.getDescriptionValues());
        mapData(map, filingHistory);

        return map;
    }

    private void mapBarcodeAndDocumentId(Map<String, Object> map, final FilingHistory filingHistory) {
        final String barcode = filingHistory.getBarcode();
        if (!StringUtils.isBlank(barcode)) {
            map.put("_barcode", barcode);
        }

        final String documentId = filingHistory.getDocumentId();
        if (!StringUtils.isBlank(documentId)) {
            map.put("_document_id", documentId);
        }
    }

    private void mapDescriptionValues(Map<String, Object> map, final DescriptionValues descriptionValues) {
        if (descriptionValues == null) {
            return;
        }

        final String resignationDate = descriptionValues.getResignationDate();
        final String officerName = descriptionValues.getOFFICERNAME();

        if (!StringUtils.isBlank(resignationDate) && !StringUtils.isBlank(officerName)) {
            Map<String, String> originalValuesMap = new HashMap<>();
            map.put("original_values", originalValuesMap);
            originalValuesMap.put("resignation_date", resignationDate);
            originalValuesMap.put("officer_name", officerName);
        }
    }

    private void mapData(Map<String, Object> map, final FilingHistory filingHistory) {
        Map<String, String> dataMap = new HashMap<>();
        map.put("data", dataMap);

        dataMap.put("type", filingHistory.getFormType());
        dataMap.put("date", filingHistory.getReceiveDate());
        dataMap.put("description", filingHistory.getDescription());
        dataMap.put("category", filingHistory.getCategory());
    }
}
