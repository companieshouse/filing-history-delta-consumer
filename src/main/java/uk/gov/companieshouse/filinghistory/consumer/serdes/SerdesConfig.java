package uk.gov.companieshouse.filinghistory.consumer.serdes;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.companieshouse.api.filinghistory.AltCapitalDescriptionValue;
import uk.gov.companieshouse.api.filinghistory.Annotation;
import uk.gov.companieshouse.api.filinghistory.AssociatedFiling;
import uk.gov.companieshouse.api.filinghistory.CapitalDescriptionValue;
import uk.gov.companieshouse.api.filinghistory.Resolution;

@Configuration
public class SerdesConfig {

    @Bean
    public JsonMapper objectMapper() {
        return JsonMapper.builderWithJackson2Defaults()
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                .build();
    }

    @Bean
    public ArrayNodeDeserialiser<String> stringArrayNodeDeserialiser(ObjectMapper objectMapper) {
        return new ArrayNodeDeserialiser<>(objectMapper, String.class);
    }

    @Bean
    public ArrayNodeDeserialiser<CapitalDescriptionValue> capitalArrayNodeDeserialiser(ObjectMapper objectMapper) {
        return new ArrayNodeDeserialiser<>(objectMapper, CapitalDescriptionValue.class);
    }

    @Bean
    public ArrayNodeDeserialiser<AltCapitalDescriptionValue> altCapitalArrayNodeDeserialiser(
            ObjectMapper objectMapper) {
        return new ArrayNodeDeserialiser<>(objectMapper, AltCapitalDescriptionValue.class);
    }

    @Bean
    public ArrayNodeDeserialiser<Annotation> annotationArrayNodeDeserialiser(ObjectMapper objectMapper) {
        return new ArrayNodeDeserialiser<>(objectMapper, Annotation.class);
    }

    @Bean
    public ArrayNodeDeserialiser<Resolution> resolutionArrayNodeDeserialiser(ObjectMapper objectMapper) {
        return new ArrayNodeDeserialiser<>(objectMapper, Resolution.class);
    }

    @Bean
    public ArrayNodeDeserialiser<AssociatedFiling> associatedFilingArrayNodeDeserialiser(ObjectMapper objectMapper) {
        return new ArrayNodeDeserialiser<>(objectMapper, AssociatedFiling.class);
    }
}
