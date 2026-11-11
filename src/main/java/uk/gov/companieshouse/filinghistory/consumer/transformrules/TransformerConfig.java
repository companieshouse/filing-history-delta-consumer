package uk.gov.companieshouse.filinghistory.consumer.transformrules;

import java.io.InputStream;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.functions.TransformerFactory;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.parsers.RuleProperties;
import uk.gov.companieshouse.filinghistory.consumer.transformrules.rules.Rule;

@Configuration
public class TransformerConfig {

    @Bean
    public TransformerService transformRules(@Value("${transform.rules}") String rulesFile,
            TransformerFactory transformerFactory) {
        ObjectMapper mapper = YAMLMapper.builder().configureForJackson2().build();
        InputStream stream = getClass().getResourceAsStream("/%s".formatted(rulesFile));
        List<RuleProperties> ruleProperties = mapper.readValue(stream, new TypeReference<>() {
        });
        List<Rule> rules = ruleProperties.stream()
                .map(ruleProperty -> ruleProperty.compile(transformerFactory))
                .toList();

        return new TransformerService(rules.getFirst(), rules.subList(1, rules.size()));
    }
}