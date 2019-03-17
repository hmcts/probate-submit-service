package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import org.springframework.core.NestedRuntimeException;
import org.springframework.http.HttpStatus;

public class CcdClientApiException extends NestedRuntimeException {

    private HttpStatus status;

    CcdClientApiException(String message, HttpStatus status) {
        super(message);
        this.status=status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
