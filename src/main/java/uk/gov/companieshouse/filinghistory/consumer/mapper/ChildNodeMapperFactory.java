package uk.gov.companieshouse.filinghistory.consumer.mapper;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.InternalData;

@Component
public class ChildNodeMapperFactory {

    private final AnnotationNodeMapper annotationMapper;

    public ChildNodeMapperFactory(AnnotationNodeMapper annotationMapper) {
        this.annotationMapper = annotationMapper;
    }

    public ChildNodeMapper getChildMapper(InternalData.TransactionKindEnum kind) {
        return switch (kind) {
            case ANNOTATION -> annotationMapper;
            default -> throw new IllegalStateException("Unexpected value: " + kind.getValue());
        };
    }
}
