package uk.gov.hmcts.probate.services.submit.validation.validator;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.ValidatorResults;
import uk.gov.hmcts.reform.probate.model.cases.caveat.CaveatData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CaveatValidatorTest {

    private List<String> errors = new ArrayList<>();
    private CaveatData caveatData;
    CaveatValidator caveatValidator;

    LocalDate afterDate = LocalDate.of(2018, 9, 12);
    LocalDate beforeDate = LocalDate.of(1954, 9, 18);

    private ProbateCaseDetails caseResponse;

    @Before
    public void setUpTest() {

        caveatValidator = new CaveatValidator();
        caveatData = CaveatCreator.createCaveatCase();
    }


    @Test
    public void shouldFailValidationWhenDodIsBeforeDob() {
        caveatData.setDeceasedDateOfBirth(afterDate);
        caveatData.setDeceasedDateOfDeath(beforeDate);
        ValidatorResults validateResults = caveatValidator.validate(caveatData);
        assertValidationErrorMessage(validateResults, "DeceasedDateOfDeath before DeceasedDateOfBirth");
    }

    @Test
    public void shouldNotRaiseErrorWhenDobIsNull() {
        caveatData.setDeceasedDateOfBirth(null);
        caveatData.setDeceasedDateOfDeath(afterDate);
        ValidatorResults validateResults = caveatValidator.validate(caveatData);
        Assertions.assertThat(validateResults.getValidationMessages()).isEmpty();
    }

    @Test
    public void shouldNotRaiseErrorWhenDodIsAfterDob() {
        caveatData.setDeceasedDateOfBirth(beforeDate);
        caveatData.setDeceasedDateOfDeath(afterDate);
        ValidatorResults validateResults = caveatValidator.validate(caveatData);
        Assertions.assertThat(validateResults.getValidationMessages()).isEmpty();
    }

    private void assertValidationErrorMessage(ValidatorResults validateResults, String validationMessage) {
        Assertions.assertThat(validateResults.getValidationMessages()).isNotEmpty();
        Assertions.assertThat(validateResults.getValidationMessages()).contains(validationMessage);
    }
}
