package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules;

import com.fasterxml.jackson.core.JsonPointer;

public class TransformerUtils {

    private TransformerUtils() {
    }

    public static JsonPointer toJsonPtr(String path) {
        return JsonPointer.compile("/%s".formatted(path.replace(".", "/")));
    }
}
