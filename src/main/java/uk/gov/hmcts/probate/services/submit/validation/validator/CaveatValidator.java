package uk.gov.hmcts.probate.services.submit.validation.validator;

import uk.gov.hmcts.probate.services.submit.validation.ValidationRule;
import uk.gov.hmcts.reform.probate.model.cases.caveat.CaveatData;

import java.util.Arrays;
import java.util.List;

public class CaveatValidator implements CaseDataValidator<CaveatData> {


    private static ValidationRule<CaveatData> isDeceasedDateOfDeathAfterDateOfBirth() {
        return ValidationRule.from(caveat -> ValidatorUtils.allValuesNotNull(caveat.getDeceasedDateOfBirth(), caveat.getDeceasedDateOfDeath()) &&
                        caveat.getDeceasedDateOfDeath().isBefore(caveat.getDeceasedDateOfBirth())
                , "DeceasedDateOfDeath before DeceasedDateOfBirth");
    }

    @Override
    public List<ValidationRule<CaveatData>> getRules() {
        return Arrays.asList(
                CaveatValidator.isDeceasedDateOfDeathAfterDateOfBirth());
    }

}
