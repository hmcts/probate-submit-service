package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.probate.model.client.CcdClientApiError;

public class CcdClientApiException extends RuntimeException {

    private HttpStatus status;
    private CcdClientApiError ccdClientApiError;

    CcdClientApiException(CcdClientApiError ccdClientApiError, HttpStatus status) {
        this.ccdClientApiError=ccdClientApiError;
        this.status=status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    CcdClientApiError getCcdClientApiError() {
        return ccdClientApiError;
    }
}
