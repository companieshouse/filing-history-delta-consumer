package uk.gov.companieshouse.filinghistory.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

	public static final String NAMESPACE = "filing-history-delta-consumer";
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
