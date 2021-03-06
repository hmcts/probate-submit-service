package uk.gov.hmcts.probate;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("file:src/smokeTest/resources/application.properties")
public class SmokeTestConfiguration {
}
