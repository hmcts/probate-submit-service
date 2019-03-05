package uk.gov.hmcts.probate.services.submit.validation;

import uk.gov.hmcts.reform.probate.model.cases.CaseData;

import java.util.function.Predicate;

public class ValidationRule<C extends CaseData> implements Validation<C> {

    private Predicate<C> predicate;
    private String message;


    public static <C extends CaseData> ValidationRule<C> from(Predicate<C> predicate, String onErrorMessage) {
        return new ValidationRule<C>(predicate, onErrorMessage);
    }

    private ValidationRule(Predicate<C> predicate, String onErrorMessage) {
        this.predicate = predicate;
        this.message = onErrorMessage;

    }

    public ValidationResult test(C caseData) {
        return predicate.test(caseData) ? ValidationResult.fail(message): ValidationResult.ok();
    }

}
