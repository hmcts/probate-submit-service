package uk.gov.hmcts.probate.services.submit.model.v2.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class NoSecurityContextException extends RuntimeException {
}
