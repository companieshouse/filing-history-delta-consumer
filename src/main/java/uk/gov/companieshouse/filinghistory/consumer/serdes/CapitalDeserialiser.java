package uk.gov.companieshouse.filinghistory.consumer.serdes;

import static uk.gov.companieshouse.filinghistory.consumer.Application.NAMESPACE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.filinghistory.AltCapitalDescriptionValue;
import uk.gov.companieshouse.api.filinghistory.CapitalDescriptionValue;
import uk.gov.companieshouse.filinghistory.consumer.exception.NonRetryableException;
import uk.gov.companieshouse.filinghistory.consumer.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class CapitalDeserialiser {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private final ObjectMapper mapper;

    public CapitalDeserialiser(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public List<CapitalDescriptionValue> deserialiseCapitalArray(ArrayNode capital) {
        try {
            return mapper.readerForListOf(CapitalDescriptionValue.class).readValue(capital);
        } catch (IOException ex) {
            LOGGER.error("Unable to deserialise capital array: [%s]".formatted(capital.toPrettyString()),
                    ex,
                    DataMapHolder.getLogMap());
            throw new NonRetryableException(
                    "Unable to deserialise capital array: [%s]".formatted(capital.toPrettyString()), ex);
        }
    }

    public List<AltCapitalDescriptionValue> deserialiseAltCapitalArray(ArrayNode altCapital) {
        try {
            return mapper.readerForListOf(AltCapitalDescriptionValue.class).readValue(altCapital);
        } catch (IOException ex) {
            LOGGER.error("Unable to deserialise alt capital array: [%s]".formatted(altCapital.toPrettyString()),
                    ex,
                    DataMapHolder.getLogMap());
            throw new NonRetryableException(
                    "Unable to deserialise alt capital array: [%s]".formatted(altCapital.toPrettyString()), ex);
        }
    }
}
