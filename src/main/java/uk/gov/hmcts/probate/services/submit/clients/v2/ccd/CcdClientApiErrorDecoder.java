package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.probate.model.client.ApiClientException;
import uk.gov.hmcts.reform.probate.model.client.ErrorResponse;

@Slf4j
public class CcdClientApiErrorDecoder implements ErrorDecoder {

    private final ResponseDecorator responseDecorator;

    public CcdClientApiErrorDecoder(ResponseDecorator responseDecorator) {
        this.responseDecorator = responseDecorator;
    }

    public CcdClientApiErrorDecoder() {
        this.responseDecorator = new ResponseDecorator(null);
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Response status: {} - {}", response.status(), response.reason());

        ErrorResponse clientErrorResponse = this.responseDecorator.mapBodyToErrorResponse();

        return new ApiClientException(response.status(), clientErrorResponse);
    }
}
