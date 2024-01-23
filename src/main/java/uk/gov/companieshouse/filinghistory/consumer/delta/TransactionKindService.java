package uk.gov.companieshouse.filinghistory.consumer.delta;

import static org.apache.commons.lang3.StringUtils.trim;

import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum;

@Component
public class TransactionKindService {

    private final String transactionIdSalt;

    public TransactionKindService(@Value("${transaction-id-salt}") String transactionIdSalt) {
        this.transactionIdSalt = transactionIdSalt;
    }

    public TransactionKindResult encodeIdByTransactionKind(TransactionKindCriteria transactionKindCriteria) {
        return new TransactionKindResult(encodeTransactionId(transactionKindCriteria.entityId()),
                TransactionKindEnum.TOP_LEVEL);
    }

    private String encodeTransactionId(String id) {
        return StringUtils.isBlank(id) ? id
                : Base64.encodeBase64URLSafeString((trim(id) + transactionIdSalt).getBytes(StandardCharsets.UTF_8));
    }
}
