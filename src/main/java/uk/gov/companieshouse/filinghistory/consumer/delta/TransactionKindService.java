package uk.gov.companieshouse.filinghistory.consumer.delta;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private final String transactionIdSalt;

    public TransactionKindService(@Value("${transaction-id-salt}") String transactionIdSalt) {
        this.transactionIdSalt = transactionIdSalt;
    }

    public TransactionKindResult encodeIdByTransactionKind(TransactionKindCriteria transactionKindCriteria) {
        final String encodedId = encodeTransactionId(transactionKindCriteria.entityId());
        DataMapHolder.get().transactionId(encodedId);
        LOGGER.debug("Transaction Kind: [%s]".formatted(transactionKindCriteria.formType()), DataMapHolder.getLogMap());
        return new TransactionKindResult(encodedId, TransactionKindEnum.TOP_LEVEL);
    }

    private String encodeTransactionId(String id) {
        return StringUtils.isBlank(id) ? id
                : Base64.encodeBase64URLSafeString((trim(id) + transactionIdSalt).getBytes(StandardCharsets.UTF_8));
    }
}
