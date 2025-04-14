package uk.gov.hmcts.probate.services.submit.model.v2.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class ConcurrentDataUpdateException extends RuntimeException {
    public ConcurrentDataUpdateException(String caseId) {
        String.format("caseId: %s not updated as working with out of date case details", caseId);
    }

}
