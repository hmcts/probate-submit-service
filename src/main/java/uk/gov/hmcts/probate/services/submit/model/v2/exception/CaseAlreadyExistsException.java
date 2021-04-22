package uk.gov.hmcts.probate.services.submit.model.v2.exception;

public class CaseAlreadyExistsException extends RuntimeException {

    public CaseAlreadyExistsException(String identifier) {
        super("Case already exists for Identifier: " + identifier);
    }
}
