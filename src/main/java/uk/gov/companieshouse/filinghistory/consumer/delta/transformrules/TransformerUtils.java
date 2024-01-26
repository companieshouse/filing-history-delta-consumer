package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules;

public class TransformerUtils {

    private TransformerUtils() {
    }

    public static String toJsonPtr(String key) {
        return "/%s".formatted(key.replace(".", "/"));
    }
}
