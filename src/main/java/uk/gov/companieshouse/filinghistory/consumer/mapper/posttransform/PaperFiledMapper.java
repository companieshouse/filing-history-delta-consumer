package uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform;

import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class PaperFiledMapper {

    private static final Pattern BARCODE_REGEX = Pattern.compile("^X");

    public boolean isPaperFiled(final String barcode) {
        return StringUtils.isBlank(barcode) || !BARCODE_REGEX.matcher(barcode).find();
    }
}
