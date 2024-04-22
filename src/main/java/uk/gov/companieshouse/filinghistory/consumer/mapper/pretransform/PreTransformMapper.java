package uk.gov.companieshouse.filinghistory.consumer.mapper.pretransform;

import static uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum.TOP_LEVEL;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.ChildProperties;
import uk.gov.companieshouse.api.delta.FilingHistory;
import uk.gov.companieshouse.api.filinghistory.InternalData.TransactionKindEnum;

@Component
public class PreTransformMapper {

    private final DeltaNodeMapper deltaNodeMapper;
    private final ChildNodeMapperFactory childNodeMapperFactory;

    public PreTransformMapper(DeltaNodeMapper deltaNodeMapper, ChildNodeMapperFactory childNodeMapperFactory) {
        this.deltaNodeMapper = deltaNodeMapper;
        this.childNodeMapperFactory = childNodeMapperFactory;
    }

    public ObjectNode mapDeltaToObjectNode(TransactionKindEnum transactionKind, FilingHistory filingHistory) {
        ObjectNode parentNode = deltaNodeMapper.mapToObjectNode(filingHistory);

        if (!TOP_LEVEL.equals(transactionKind) || hasChildArray(filingHistory.getChild())) {
            parentNode = childNodeMapperFactory.getChildMapper(transactionKind)
                    .mapChildObjectNode(filingHistory, parentNode);
        }
        return parentNode;
    }

    private static boolean hasChildArray(List<ChildProperties> childArray) {
        return childArray != null && !childArray.isEmpty();
    }
}