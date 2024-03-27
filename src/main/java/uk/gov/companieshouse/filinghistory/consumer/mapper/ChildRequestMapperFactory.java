package uk.gov.companieshouse.filinghistory.consumer.mapper;

import org.springframework.stereotype.Component;

@Component
public class ChildRequestMapperFactory {

    private final AnnotationRequestMapper annotationRequestMapper;

    public ChildRequestMapperFactory(AnnotationRequestMapper annotationRequestMapper) {
        this.annotationRequestMapper = annotationRequestMapper;
    }

    public ChildRequestMapper getChildRequestMapper(String type) {
        return switch (type) {
            case "annotation" -> annotationRequestMapper;
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }
}
