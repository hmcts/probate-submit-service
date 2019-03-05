package uk.gov.hmcts.probate.services.submit.validation;

@FunctionalInterface
public interface Validation<K> {

	ValidationResult test(K param);

}
