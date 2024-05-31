package uk.gov.companieshouse.filinghistory.consumer.kafka;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.ValueMatcher;
import uk.gov.companieshouse.api.filinghistory.InternalFilingHistoryApi;

public class PutRequestDescriptionMatcher implements ValueMatcher<Request> {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .setSerializationInclusion(Include.NON_EMPTY)
            .registerModule(new JavaTimeModule());

    private final String expectedDescription;
    private final String delta;
    private boolean logged = false;

    public PutRequestDescriptionMatcher(String expectedDescription, String delta) {
        this.expectedDescription = expectedDescription;
        this.delta = delta;
    }

    @Override
    public MatchResult match(Request value) {
        return matchBody(value.getBodyAsString());
    }

    private MatchResult matchBody(String actualBody) {

        try {
            InternalFilingHistoryApi actual = MAPPER.readValue(actualBody, InternalFilingHistoryApi.class);

            String actualDescription = getDescription(actual);

            MatchResult result = MatchResult.of(expectedDescription.equals(actualDescription));
            if (!result.isExactMatch() && !logged) {
                logged = true;
                System.out.println("\n"
                        + "Expected description: "
                        + expectedDescription
                        + "\n"
                        + "Actual description:   "
                        + actualDescription
                        + "\n"
                        + "Delta: "
                        + delta
                        + "\n"
                        + "Request body: "
                        + actualBody
                        + "\n\n");
            }
            return result;
        } catch (JsonProcessingException ex) {
            return MatchResult.of(false);
        }
    }

    private static String getDescription(InternalFilingHistoryApi putRequest) {

        if (putRequest.getExternalData().getResolutions() != null) {
            return putRequest.getExternalData().getResolutions().getFirst().getDescription();
        } else if (putRequest.getExternalData().getAnnotations() != null) {
            return putRequest.getExternalData().getAnnotations().getFirst().getDescription();
        } else if (putRequest.getExternalData().getAssociatedFilings() != null) {
            return putRequest.getExternalData().getAssociatedFilings().getFirst().getDescription();
        } else {
            return putRequest.getExternalData().getDescription();
        }
    }
}
