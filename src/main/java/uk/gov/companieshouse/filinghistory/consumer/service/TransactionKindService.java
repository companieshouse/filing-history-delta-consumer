package uk.gov.companieshouse.filinghistory.consumer.service;

import static org.apache.commons.lang3.StringUtils.trim;
import static uk.gov.companieshouse.filinghistory.consumer.Application.NAMESPACE;

import java.nio.charset.StandardCharsets;
import java.util.List;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private static final String ANNOTATION_FORM_TYPE = "ANNOTATION";
    private static final String ASSOCIATED_FILING_FORM_TYPE = "ASSOCIATED-FILING";

    private final FormTypeService formTypeService;
    private final String transactionIdSalt;

    public TransactionKindService(FormTypeService formTypeService, @Value("${transaction-id-salt}") String transactionIdSalt) {
        this.formTypeService = formTypeService;
        this.transactionIdSalt = transactionIdSalt;
    }

    public TransactionKindResult encodeIdByTransactionKind(TransactionKindCriteria transactionKindCriteria) {
        LOGGER.debug("Transaction Kind: [%s]".formatted(transactionKindCriteria.formType()), DataMapHolder.getLogMap());

        final String formType = formTypeService.getFormType(transactionKindCriteria);

        final String encodedId;
        final TransactionKindEnum kindEnum;
        switch (formType) {
            case ANNOTATION_FORM_TYPE -> {
                encodedId = encodeTransactionId(transactionKindCriteria.parentEntityId());
                kindEnum = TransactionKindEnum.ANNOTATION; // encode by parent entity id
            }
            case ASSOCIATED_FILING_FORM_TYPE -> {
                encodedId = encodeTransactionId(transactionKindCriteria.parentEntityId());
                kindEnum = TransactionKindEnum.ASSOCIATED_FILING; // encode by parent entity id
            }
            default -> {
                encodedId = encodeTransactionId(transactionKindCriteria.entityId());
                kindEnum = TransactionKindEnum.TOP_LEVEL; // encode by entity id
            }
        }
        return new TransactionKindResult(encodedId, kindEnum);
    }

    public String encodeTransactionId(String id) {
        String encodedId = StringUtils.isBlank(id) ? id
                : Base64.encodeBase64URLSafeString((trim(id) + transactionIdSalt).getBytes(StandardCharsets.UTF_8));
        DataMapHolder.get().transactionId(encodedId);
        return encodedId;
    }
}
