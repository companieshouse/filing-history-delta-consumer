package uk.gov.companieshouse.filinghistory.consumer.mapper;

import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class PaperFiledMapper {

    private static final Pattern BARCODE_REGEX = Pattern.compile("^X");
    private static final Pattern DOCUMENT_ID_REGEX = Pattern.compile("^...X", Pattern.CASE_INSENSITIVE);

    public boolean isPaperFiled(final String barcode, final String documentId) {
        if (StringUtils.isNotBlank(barcode)) {
            return !BARCODE_REGEX.matcher(barcode).find();
        }
        if (StringUtils.isNotBlank(documentId)) {
            return !DOCUMENT_ID_REGEX.matcher(documentId).find();
        }
        return true;
    }
}
