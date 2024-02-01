package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.AddressCase;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.BsonDate;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.CapitalCaptor;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.FormatNumber;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.LowerCase;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.ProcessCapital;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.ReplaceProperty;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.SentenceCase;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.TitleCase;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.TransformerFactory;

public class TransformerTestingUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .registerModule(new JavaTimeModule());

    private static final BsonDate BSON_DATE = new BsonDate(MAPPER);
    private static final LowerCase LOWER_CASE = new LowerCase(MAPPER);
    private static final SentenceCase SENTENCE_CASE = new SentenceCase(MAPPER);
    private static final TitleCase TITLE_CASE = new TitleCase(MAPPER);
    private static final ReplaceProperty REPLACE_PROPERTY = new ReplaceProperty(MAPPER, LOWER_CASE);
    private static final ProcessCapital PROCESS_CAPITAL = new ProcessCapital(MAPPER, new CapitalCaptor(MAPPER, new FormatNumber()));
    private static final AddressCase ADDRESS_CASE = new AddressCase(MAPPER, TITLE_CASE);

    private static final TransformerFactory TRANSFORMER_FACTORY = new TransformerFactory(ADDRESS_CASE, BSON_DATE,
            LOWER_CASE, SENTENCE_CASE, TITLE_CASE, REPLACE_PROPERTY, PROCESS_CAPITAL);

    private TransformerTestingUtils() {
    }

    public static TransformerFactory getTransformerFactory() {
        return TRANSFORMER_FACTORY;
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    public static BsonDate getBsonDate() {
        return BSON_DATE;
    }

    public static LowerCase getLowerCase() {
        return LOWER_CASE;
    }

    public static SentenceCase getSentenceCase() {
        return SENTENCE_CASE;
    }

    public static TitleCase getTitleCase() {
        return TITLE_CASE;
    }

    public static ReplaceProperty getReplaceProperty() {
        return REPLACE_PROPERTY;
    }

    public static ProcessCapital getProcessCapital() {
        return PROCESS_CAPITAL;
    }

    public static AddressCase getAddressCase() {
        return ADDRESS_CASE;
    }
}
