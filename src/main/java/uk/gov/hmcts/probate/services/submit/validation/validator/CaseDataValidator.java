package uk.gov.hmcts.probate.services.submit.validation.validator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.probate.services.submit.validation.ValidationRule;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.ValidatorResults;

import java.util.List;

@Component
public interface CaseDataValidator<C extends CaseData> {


    public default ValidatorResults validate(C caseData) {
        ValidatorResults results = new ValidatorResults();
        getRules().stream().map(rule -> rule.test(caseData)).filter(validationResult -> !validationResult.isValid()).forEach(validationResult -> results.getValidationMessages().add(validationResult.getMessage()));
        return results;

    }

    public List<ValidationRule<C>> getRules();
}
