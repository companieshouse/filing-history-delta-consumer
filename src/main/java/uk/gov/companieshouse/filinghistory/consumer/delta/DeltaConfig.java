package uk.gov.companieshouse.filinghistory.consumer.delta;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.function.Supplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.sdk.manager.ApiClientManager;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@Configuration
public class DeltaConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .registerModule(new JavaTimeModule());
    }

    @Bean
    Supplier<InternalApiClient> internalApiClientSupplier() {
        return ApiClientManager::getPrivateSDKLocalURL;
    }
}
