package uk.gov.companieshouse.filinghistory.consumer.transformrules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.TransformerFactory;

class TransformerServiceTest {

    private static final ObjectMapper MAPPER = TransformerTestingUtils.getMapper();
    private static final String ENTITY_ID = "entityId";
    private static final String TM01_REQUEST_BODY = """
            {
                "_id" : "mongo_id",
                "company_number" : "12345678",
                "data" : {
                    "description" : "Appointment Terminated, Director JOE BLOGS",
                    "category" : "officer",
                    "date" : "20091007140721",
                    "type" : "TM01"
                },
                "_entity_id" : "0123456789",
                "_barcode" : "AAAAAAAA",
                "_document_id" : "000AAAAAAAA1234",
                "original_values": {
                    "resignation_date": "06/08/2011",
                    "officer_name": "Joe Blogs"
                }
            }""";

    private static final String AA_REQUEST_BODY = """
            {
                "_id" : "MTUxMjg0MTM5YWRpcXprY3g",
                "company_number" : "14388379",
                "_document_id" : "000AOKK2A9N0981",
                "_entity_id" : "151284139",
                "data" : {
                    "date" : "19981012115917",
                    "description" : "GROUP ACCOUNTS FOR SMALL CO. MADE UP TO 05/04/98",
                    "type" : "AA"
                }
            }""";

    private static final String REQUEST_WITH_DEFINE_EXEC = """
            {
                "_id" : "MzA0MTc3MzgzMmFkaXF6a2N4",
                "_entity_id" : "3041773832",
                "data" : {
                    "type" : "SH01",
                    "description" : "29/07/11 Statement of Capital gbp 13337"
                }
            }
            """;

    private static final String REQUEST_WITH_TWO_LIKE_CAPTURE_GROUPS = """
            {
                "_id" : "MzA0MTc3MzgzMmFkaXF6a2N4",
                "_entity_id" : "3041773832",
                "data" : {
                    "type" : "AA01",
                    "description" : "PREVSHO FROM 29/07/11 TO 1/07/12"
                }
            }
            """;

    private static final String MATCH_DEFAULT_RULE = """
            {
                "_id" : "MzA0MTc3MzgzMmFkaXF6a2N4",
                "_entity_id" : "3041773832",
                "data" : {
                    "description" : "SOME unrecognised string"
                }
            }
            """;

    private final TransformerFactory transformerFactory = TransformerTestingUtils.getTransformerFactory();
    private TransformerService service;

    @BeforeEach
    void setUp() throws IOException {
        service = new TransformerConfig().transformRules("transform_rules.yml", transformerFactory);
    }

    @Test
    void transformTM01() throws JsonProcessingException {
        // given
        JsonNode delta = MAPPER.readTree(TM01_REQUEST_BODY);

        ObjectNode expected = MAPPER.createObjectNode();

        expected
                .put("_id", "mongo_id")
                .put("company_number", "12345678")
                .put("_entity_id", "0123456789")
                .put("_barcode", "AAAAAAAA")
                .put("_document_id", "000AAAAAAAA1234")
                .put("original_description", "Appointment Terminated, Director joe blogs");

        expected
                .putObject("data")
                .put("description", "termination-director-company-with-name-termination-date")
                .put("category", "officers")
                .put("subcategory", "termination")
                .put("date", "20091007140721")
                .put("action_date", "2011-08-06T00:00:00Z")
                .put("type", "TM01")
                .putObject("description_values")
                .put("officer_name", "Joe Blogs")
                .put("termination_date", "2011-08-06T00:00:00Z");

        expected
                .putObject("original_values")
                .put("resignation_date", "06/08/2011")
                .put("officer_name", "Joe Blogs");

        // when
        JsonNode actual = service.transform(delta, ENTITY_ID);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void transformAA() throws JsonProcessingException {
        // given
        JsonNode delta = MAPPER.readTree(AA_REQUEST_BODY);

        ObjectNode expected = MAPPER.createObjectNode();
        expected
                .put("_id", "MTUxMjg0MTM5YWRpcXprY3g")
                .put("company_number", "14388379")
                .put("_entity_id", "151284139")
                .put("_document_id", "000AOKK2A9N0981")
                .put("original_description", "Group accounts for small co. Made up to 05/04/98");

        expected
                .putObject("data")
                .put("description", "accounts-with-accounts-type-small-group")
                .put("category", "accounts")
                .put("date", "19981012115917")
                .put("action_date", "1998-04-05T00:00:00Z")
                .put("type", "AA")
                .putObject("description_values")
                .put("made_up_date", "1998-04-05T00:00:00Z");

        // when
        JsonNode actual = service.transform(delta, ENTITY_ID);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void transformWithDefineAndExec() throws JsonProcessingException {
        // given
        JsonNode delta = MAPPER.readTree(REQUEST_WITH_DEFINE_EXEC);

        ObjectNode expected = MAPPER.createObjectNode();
        expected
                .put("_id", "MzA0MTc3MzgzMmFkaXF6a2N4")
                .put("_entity_id", "3041773832")
                .put("original_description", "29/07/11 Statement of Capital gbp 13337");

        ObjectNode capital = MAPPER.createObjectNode();
        capital
                .put("figure", "13,337")
                .put("currency", "GBP");

        expected
                .putObject("data")
                .put("description", "capital-allotment-shares")
                .put("category", "capital")
                .put("action_date", "2011-07-29T00:00:00Z")
                .put("type", "SH01")
                .putObject("description_values")
                .put("date", "2011-07-29T00:00:00Z")
                .putArray("capital")
                .add(capital);

        // when
        JsonNode actual = service.transform(delta, ENTITY_ID);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void transformAA01() throws JsonProcessingException {
        // given
        JsonNode delta = MAPPER.readTree(REQUEST_WITH_TWO_LIKE_CAPTURE_GROUPS);

        ObjectNode expected = MAPPER.createObjectNode();
        expected
                .put("_id", "MzA0MTc3MzgzMmFkaXF6a2N4")
                .put("_entity_id", "3041773832")
                .put("original_description", "Prevsho from 29/07/11 to 1/07/12");

        expected
                .putObject("data")
                .put("description", "change-account-reference-date-company-previous-shortened")
                .put("category", "accounts")
                .put("action_date", "2012-07-01T00:00:00Z")
                .put("type", "AA01")
                .putObject("description_values")
                .put("made_up_date", "2011-07-29T00:00:00Z")
                .put("new_date", "2012-07-01T00:00:00Z");

        // when
        JsonNode actual = service.transform(delta, ENTITY_ID);

        // then
        assertEquals(expected, actual);
    }


    @Test
    void transformUsingDefaultRule() throws JsonProcessingException {
        // given
        JsonNode delta = MAPPER.readTree(MATCH_DEFAULT_RULE);

        ObjectNode expected = MAPPER.createObjectNode();
        expected
                .put("_id", "MzA0MTc3MzgzMmFkaXF6a2N4")
                .put("_entity_id", "3041773832")
                .put("original_description", "Some unrecognised string")
                .put("matched_default", "1");

        expected
                .putObject("data")
                .put("description", "legacy")
                .putObject("description_values")
                .put("description", "Some unrecognised string");

        // when
        JsonNode actual = service.transform(delta, ENTITY_ID);

        // then
        assertEquals(expected, actual);
    }
}