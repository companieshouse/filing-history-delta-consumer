package uk.gov.companieshouse.filinghistory.consumer.delta.transformrules;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.functions.TransformerFactory;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.parsers.RuleProperties;
import uk.gov.companieshouse.filinghistory.consumer.delta.transformrules.rules.Rule;

@Configuration
public class TransformerConfig {

    @Bean
    public TransformerService transformRules(@Value("${transform.rules}") String rulesFile,
            TransformerFactory transformerFactory) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        InputStream stream = getClass().getResourceAsStream("/%s".formatted(rulesFile));
        List<RuleProperties> ruleProperties = mapper.readValue(stream, new TypeReference<>() {
        });
        List<Rule> rules = ruleProperties.stream()
                .map(ruleProperty -> ruleProperty.compile(transformerFactory))
                .toList();

        return new TransformerService(rules.getFirst(), rules.subList(1, rules.size()));
    }
}