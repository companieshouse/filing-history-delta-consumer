package uk.gov.companieshouse.filinghistory.consumer.delta;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.delta.DescriptionValues;
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.api.delta.FilingHistoryDelta;

class PreTransformMapperTest {

    private final PreTransformMapper preTransformMapper = new PreTransformMapper();

    @Test
    void shouldMapDeltaObjectOntoHashMap() {
        // given
        final FilingHistoryDelta delta = new FilingHistoryDelta()
                .deltaAt("20140916230459600643")
                .filingHistory(List.of(
                        new FilingHistory()
                                .category("2")
                                .receiveDate("20110905053919")
                                .formType("TM01")
                                .description("Appointment Terminated, Director JOHN DOE")
                                .barcode("XAITVXAX")
                                .documentId("000XAITVXAX4682")
                                .companyNumber("12345678")
                                .entityId("3063732185")
                                .parentEntityId("")
                                .parentFormType("")
                                .descriptionValues(new DescriptionValues()
                                        .resignationDate("02/07/2011")
                                        .OFFICER_NAME("John Doe"))
                                .preScannedBatch("0")
                ));

        Map<String, Object> expectedHashMap = new HashMap<>();
        expectedHashMap.put("company_number", "12345678");
        expectedHashMap.put("_entity_id", "3063732185");
        expectedHashMap.put("_barcode", "XAITVXAX");
        expectedHashMap.put("_document_id", "000XAITVXAX4682");

        Map<String, String> expectedOriginalValuesHashMap = new HashMap<>();
        expectedHashMap.put("original_values", expectedOriginalValuesHashMap);

        Map<String, String> expectedDataHashMap = new HashMap<>();
        expectedHashMap.put("data", expectedDataHashMap);

        expectedOriginalValuesHashMap.put("resignation_date", "02/07/2011");
        expectedOriginalValuesHashMap.put("officer_name", "John Doe");

        expectedDataHashMap.put("type", "TM01");
        expectedDataHashMap.put("date", "20110905053919");
        expectedDataHashMap.put("description", "Appointment Terminated, Director JOHN DOE");
        expectedDataHashMap.put("category", "2");

        // when
        final Map<String, Object> actualHashMap = preTransformMapper.map(delta);

        // then
        assertEquals(expectedHashMap, actualHashMap);
    }

    @Test
    void shouldMapDeltaObjectOntoHashMapWhenDeltaMissingFieldsAndNullDescriptionValues() {
        // given
        final FilingHistoryDelta delta = new FilingHistoryDelta()
                .deltaAt("20140916230459600643")
                .filingHistory(List.of(
                        new FilingHistory()
                                .category("2")
                                .receiveDate("20110905053919")
                                .formType("TM01")
                                .description("Appointment Terminated, Director JOHN DOE")
                                .barcode("")
                                .documentId("")
                                .descriptionValues(null)
                                .companyNumber("12345678")
                                .entityId("3063732185")
                                .parentEntityId("")
                                .parentFormType("")
                                .preScannedBatch("0")
                ));

        Map<String, Object> expectedHashMap = new HashMap<>();
        expectedHashMap.put("company_number", "12345678");
        expectedHashMap.put("_entity_id", "3063732185");

        Map<String, String> expectedDataHashMap = new HashMap<>();
        expectedHashMap.put("data", expectedDataHashMap);

        expectedDataHashMap.put("type", "TM01");
        expectedDataHashMap.put("date", "20110905053919");
        expectedDataHashMap.put("description", "Appointment Terminated, Director JOHN DOE");
        expectedDataHashMap.put("category", "2");

        // when
        final Map<String, Object> actualHashMap = preTransformMapper.map(delta);

        // then
        assertEquals(expectedHashMap, actualHashMap);
    }

    @Test
    void shouldMapDeltaObjectOntoHashMapWhenDeltaMissingFieldsAndEmptyDescriptionValuesObject() {
        // given
        final FilingHistoryDelta delta = new FilingHistoryDelta()
                .deltaAt("20140916230459600643")
                .filingHistory(List.of(
                        new FilingHistory()
                                .category("2")
                                .receiveDate("20110905053919")
                                .formType("TM01")
                                .description("Appointment Terminated, Director JOHN DOE")
                                .barcode("")
                                .documentId("")
                                .descriptionValues(new DescriptionValues())
                                .companyNumber("12345678")
                                .entityId("3063732185")
                                .parentEntityId("")
                                .parentFormType("")
                                .preScannedBatch("0")
                ));

        Map<String, Object> expectedHashMap = new HashMap<>();
        expectedHashMap.put("company_number", "12345678");
        expectedHashMap.put("_entity_id", "3063732185");

        Map<String, String> expectedDataHashMap = new HashMap<>();
        expectedHashMap.put("data", expectedDataHashMap);

        expectedDataHashMap.put("type", "TM01");
        expectedDataHashMap.put("date", "20110905053919");
        expectedDataHashMap.put("description", "Appointment Terminated, Director JOHN DOE");
        expectedDataHashMap.put("category", "2");

        // when
        final Map<String, Object> actualHashMap = preTransformMapper.map(delta);

        // then
        assertEquals(expectedHashMap, actualHashMap);
    }
}
