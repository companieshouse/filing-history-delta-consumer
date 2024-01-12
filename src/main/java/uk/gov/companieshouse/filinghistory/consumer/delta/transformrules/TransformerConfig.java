package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.parsers.RuleProperties;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.rules.Rule;

@Configuration
public class TransformerConfig {

    @Bean
    public TransformerService transformRules(@Value("${transform.rules}") String rulesFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        File file = new ClassPathResource(rulesFile).getFile();
        List<RuleProperties> ruleProperties = mapper.readValue(file, new TypeReference<>() {});

        List<Rule> rules = ruleProperties.stream()
                .map(RuleProperties::compile)
                .toList();

        return new TransformerService(rules.getFirst(), rules.subList(1, rules.size()));
    }
}