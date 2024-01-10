package uk.gov.companieshouse.filinghistory.consumer.transformer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransformerServiceTest {

    private static final String tm01RequestBody = """
            {
                "_id" : "mongo_id",
                "company_number" : "12345678",
                "data" : {
                    "description" : "Appointment Terminated, Director JOE BLOGS",
                    "category" : "officer",
                    "date" : 20091007140721,
                    "type" : "TM01"
                },
                "_entity_id" : "0123456789",
                "_barcode" : "AAAAAAAA",
                "_document_id" : "000AAAAAAAA1234"
            }""";

    private TransformerService service;

    @BeforeEach
    void setUp() throws IOException {
        service = new TransformerConfig().transformRules("transform_rules.yml");
    }

    @Test
    void transform() throws JsonProcessingException {
        // given
        ObjectMapper mapper = new ObjectMapper();
        JsonNode delta = mapper.readTree(tm01RequestBody);

        // when
        JsonNode requestBody = service.transform(delta);

        // then
        assertNotNull(requestBody);
        System.out.println(requestBody);
    }
}