package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.AddressCase;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.BsonDate;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.LowerCase;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.ProcessCapital;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.ReplaceProperty;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.SentenceCase;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.TitleCase;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.TransformerFactory;

class TransformerServiceTest {

    private static final String TM01_REQUEST_BODY = """
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
    private static final String AA_REQUEST_BODY = """
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
                    "type" : "DEFAULT",
                    "description" : "Some unrecognised string"
                }
            }
            """;

    private final TransformerFactory transformerFactory = new TransformerFactory(new AddressCase(),
            new BsonDate(), new LowerCase(), new SentenceCase(), new TitleCase(),
            new ReplaceProperty(), new ProcessCapital());
    private TransformerService service;

    @BeforeEach
    void setUp() throws IOException {
        service = new TransformerConfig().transformRules("transform_rules.yml", transformerFactory);
    }

    @Test
    void transformTM01() throws JsonProcessingException {
        // given
        ObjectMapper mapper = new ObjectMapper();
        JsonNode delta = mapper.readTree(TM01_REQUEST_BODY);

        // when
        JsonNode requestBody = service.transform(delta);

        // then
        assertNotNull(requestBody);
    }

    @Test
    void transformAA() throws JsonProcessingException {
        // given
        ObjectMapper mapper = new ObjectMapper();
        JsonNode delta = mapper.readTree(AA_REQUEST_BODY);

        // when
        JsonNode requestBody = service.transform(delta);

        // then
        assertNotNull(requestBody);
        System.out.println(requestBody);
    }

    @Test
    void transformWithDefineAndExec() throws JsonProcessingException {
        // given
        ObjectMapper mapper = new ObjectMapper();
        JsonNode delta = mapper.readTree(REQUEST_WITH_DEFINE_EXEC);

        // when
        JsonNode requestBody = service.transform(delta);

        // then
        assertNotNull(requestBody);
    }

    @Test
    void transformAA01() throws JsonProcessingException {
        // given
        ObjectMapper mapper = new ObjectMapper();
        JsonNode delta = mapper.readTree(REQUEST_WITH_TWO_LIKE_CAPTURE_GROUPS);

        // when
        JsonNode requestBody = service.transform(delta);

        // then
        assertNotNull(requestBody);
        System.out.println(requestBody);
    }


    @Test
    void transformUsingDefaultRule() throws JsonProcessingException {
        // given
        ObjectMapper mapper = new ObjectMapper();
        JsonNode delta = mapper.readTree(MATCH_DEFAULT_RULE);

        // when
        JsonNode requestBody = service.transform(delta);

        // then
        assertNotNull(requestBody);
        System.out.println(requestBody);
    }
}