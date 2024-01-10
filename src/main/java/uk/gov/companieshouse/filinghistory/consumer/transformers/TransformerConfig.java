package uk.gov.companieshouse.filinghistory.consumer.transformers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.filinghistory.consumer.transformers.parsers.RuleProperties;

@Configuration
public class TransformerConfig {

    @Resource(name = "transform_rules.yaml")
    private File transformRulesYaml;

    @Bean
    public TransformRules transformerRules() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        List<RuleProperties> rules = mapper.readValue(transformRulesYaml, new TypeReference<>() {
        });

        List<TransformRule> compiledRules = rules.stream()
                .map(RuleProperties::compile)
                .toList();

        return new TransformRules(compiledRules);
    }
}