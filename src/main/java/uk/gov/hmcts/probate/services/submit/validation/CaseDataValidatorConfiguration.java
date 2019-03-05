package uk.gov.hmcts.probate.services.submit.validation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.probate.services.submit.validation.validator.CaveatValidator;
import uk.gov.hmcts.probate.services.submit.validation.validator.IntestacyValidator;

@Configuration
public class CaseDataValidatorConfiguration {

    @Bean
    public IntestacyValidator intestacyValidator() {
        return new IntestacyValidator();
    }

    @Bean
    public CaveatValidator caveatValidator() {
        return new CaveatValidator();
    }
}
