package uk.gov.companieshouse.filinghistory.consumer.transformrules;

import tools.jackson.core.JsonPointer;

public class TransformerUtils {

    private TransformerUtils() {
    }

    public static JsonPointer toJsonPtr(String path) {
        return JsonPointer.compile("/%s".formatted(path.replace(".", "/")));
    }
}
