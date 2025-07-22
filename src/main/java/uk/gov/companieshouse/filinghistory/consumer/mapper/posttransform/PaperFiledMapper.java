package uk.gov.companieshouse.filinghistory.consumer.mapper.posttransform;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class PaperFiledMapper {

    private static final List<String> ANNOTATION_PARENT_TYPES = Arrays.asList("AA", "AAMD");
    private static final String ANNOTATION = "ANNOTATION";
    private static final Pattern BARCODE_REGEX = Pattern.compile("^X");

    //To prevent non-paper-filed transactions from being converted into paper-filed transactions 
    //when an annotation is applied to the parent transaction. 
    //ANNOTATION and it's parentFormType is either AA or AAMD is checked for.
    public boolean isPaperFiled(final String barcode, final String formType, String parentFormType) {
        boolean isAnnotation = ANNOTATION.equals(formType) && ANNOTATION_PARENT_TYPES.contains(parentFormType);
        return !isAnnotation && (StringUtils.isBlank(barcode) || !BARCODE_REGEX.matcher(barcode).find());
    }
}
