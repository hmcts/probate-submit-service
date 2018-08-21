package uk.gov.hmcts.probate.services.submit.controllers;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@Configuration
@ComponentScan(basePackages = "uk.gov.hmcts.probate.services.submit.controllers")
public class ControllerConfiguration {
}
