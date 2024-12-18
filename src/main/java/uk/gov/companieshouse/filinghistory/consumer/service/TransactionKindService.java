package uk.gov.companieshouse.filinghistory.consumer.service;

import static org.apache.commons.lang3.StringUtils.trim;
import static uk.gov.companieshouse.filinghistory.consumer.Application.NAMESPACE;

import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum;
import uk.gov.companieshouse.filinghistory.consumer.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class TransactionKindService {
    private static final String ANNOTATION = "ANNOTATION";
    private static final String RES_15 = "RES15";

    private final FormTypeService formTypeService;
    private final String transactionIdSalt;

    public TransactionKindService(FormTypeService formTypeService,
            @Value("${transaction-id-salt}") String transactionIdSalt) {
        this.formTypeService = formTypeService;
        this.transactionIdSalt = transactionIdSalt;
    }

    public TransactionKindResult encodeIdByTransactionKind(TransactionKindCriteria kindCriteria) {
        final String encodedId;
        final TransactionKindEnum kindEnum;
        if (ANNOTATION.equals(kindCriteria.formType())) {
            if (StringUtils.isNotBlank(kindCriteria.parentEntityId())) {
                encodedId = encodeTransactionId(kindCriteria.parentEntityId());
            } else {
                encodedId = encodeTransactionId(kindCriteria.entityId());
            }
            kindEnum = TransactionKindEnum.ANNOTATION;

        } else if (formTypeService.isResolutionType(kindCriteria.formType())) {
            if (StringUtils.isNotBlank(kindCriteria.barcode()) && !RES_15.equals(kindCriteria.formType())) {
                encodedId = encodeTransactionId(kindCriteria.barcode());
            } else if (StringUtils.isNotBlank(kindCriteria.parentEntityId())) {
                encodedId = encodeTransactionId(kindCriteria.parentEntityId());
            } else {
                encodedId = encodeTransactionId(kindCriteria.entityId());
            }
            kindEnum = TransactionKindEnum.RESOLUTION;

        } else if (!formTypeService.isAssociatedFilingBlockListed(kindCriteria)
                && StringUtils.isNotBlank(kindCriteria.parentEntityId())) {
            encodedId = encodeTransactionId(kindCriteria.parentEntityId());
            kindEnum = TransactionKindEnum.ASSOCIATED_FILING;

        } else {
            encodedId = encodeTransactionId(kindCriteria.entityId());
            kindEnum = TransactionKindEnum.TOP_LEVEL;
        }
        return new TransactionKindResult(encodedId, kindEnum);
    }

    private String encodeTransactionId(String id) {
        String encodedId = StringUtils.isBlank(id) ? id
                : Base64.encodeBase64URLSafeString((trim(id) + transactionIdSalt).getBytes(StandardCharsets.UTF_8));
        DataMapHolder.get().transactionId(encodedId);
        return encodedId;
    }
}
