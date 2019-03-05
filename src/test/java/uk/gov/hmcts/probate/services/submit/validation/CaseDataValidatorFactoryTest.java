package uk.gov.hmcts.probate.services.submit.validation;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.probate.services.submit.validation.validator.CaseDataValidator;
import uk.gov.hmcts.probate.services.submit.validation.validator.CaveatCreator;
import uk.gov.hmcts.probate.services.submit.validation.validator.CaveatValidator;
import uk.gov.hmcts.probate.services.submit.validation.validator.GrantOfRepresentationCreator;
import uk.gov.hmcts.probate.services.submit.validation.validator.IntestacyValidator;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataValidatorFactoryTest {

    private CaseDataValidatorFactory caseDataValidatorFactory;

    @Mock
    private IntestacyValidator intestacyValidator;

    @Mock
    private CaveatValidator caveatValidator;


    @Before
    public void setUpTest() {
        caseDataValidatorFactory = new CaseDataValidatorFactory(intestacyValidator, caveatValidator);
    }

    @Test
    public void shouldReturnIntestacyValidator() {
        CaseData caseData = GrantOfRepresentationCreator.createIntestacyCase();
        Optional<CaseDataValidator> validator = caseDataValidatorFactory.getValidator(caseData);
        Assertions.assertTrue(validator.isPresent());
        Assertions.assertTrue(validator.get() instanceof IntestacyValidator);
    }

    @Test
    public void shouldReturnCaveatValidator() {
        CaseData caseData = CaveatCreator.createCaveatCase();
        Optional<CaseDataValidator> validator = caseDataValidatorFactory.getValidator(caseData);
        Assertions.assertTrue(validator.isPresent());
        Assertions.assertTrue(validator.get() instanceof CaveatValidator);
    }

}
