package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules;

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

    // TODO The dates might not have the correct structure
    private static final String aaRequestBody = """
            {
                "_id" : "MTUxMjg0MTM5YWRpcXprY3g",
                "company_number" : "14388379",
                "_document_id" : "000AOKK2A9N0981",
                "_entity_id" : "151284139",
                "data" : {
                    "action_date" : "1998-04-05T00:00:00.000+0000",
                    "category" : "accounts",
                    "date" : "1998-10-12T11:59:17.000+0000",
                    "description" : "GROUP ACCOUNTS FOR SMALL CO. MADE UP TO 05/04/98",
                    "paper_filed" : true,
                    "type" : "AA"
                }
            }""";

    private TransformerService service;

    @BeforeEach
    void setUp() throws IOException {
        service = new TransformerConfig().transformRules("transform_rules.yml");
    }

    @Test
    void transformTM01() throws JsonProcessingException {
        // given
        ObjectMapper mapper = new ObjectMapper();
        JsonNode delta = mapper.readTree(tm01RequestBody);

        // when
        JsonNode requestBody = service.transform(delta);

        // then
        assertNotNull(requestBody);
    }

    @Test
    void transformAA() throws JsonProcessingException {
        // given
        ObjectMapper mapper = new ObjectMapper();
        JsonNode delta = mapper.readTree(aaRequestBody);

        // when
        JsonNode requestBody = service.transform(delta);

        // then
        assertNotNull(requestBody);
    }
}