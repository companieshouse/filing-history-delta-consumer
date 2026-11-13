package uk.gov.companieshouse.filinghistory.consumer.serdes;

import static uk.gov.companieshouse.filinghistory.consumer.Application.NAMESPACE;

import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import uk.gov.companieshouse.filinghistory.consumer.exception.NonRetryableException;
import uk.gov.companieshouse.filinghistory.consumer.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

public class ArrayNodeDeserialiser<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private final ObjectMapper mapper;
    private final Class<T> type;

    protected ArrayNodeDeserialiser(ObjectMapper mapper, Class<T> type) {
        this.mapper = mapper;
        this.type = type;
    }

    public List<T> deserialise(ArrayNode arrayNode) {
        try {
            return mapper.readerForListOf(type).readValue(arrayNode);
        } catch (JacksonException ex) {
            LOGGER.error("Unable to deserialise array node: [%s]".formatted(arrayNode.toPrettyString()),
                    ex,
                    DataMapHolder.getLogMap());
            throw new NonRetryableException(
                    "Unable to deserialise array node: [%s]".formatted(arrayNode.toPrettyString()), ex);
        }
    }
}
