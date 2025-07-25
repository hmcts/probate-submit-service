package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.probate.model.client.ApiClientException;
import uk.gov.hmcts.reform.probate.model.client.ErrorResponse;

@Slf4j
public class CcdClientApiErrorDecoder implements ErrorDecoder {

    private ObjectMapper objectMapper;

    public CcdClientApiErrorDecoder() {
    }

    public CcdClientApiErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Response status: {} - {}", response.status(), response.reason());

        ResponseDecorator responseDecorator = new ResponseDecorator(response, this.objectMapper);
        ErrorResponse clientErrorResponse = responseDecorator.mapBodyToErrorResponse();

        return new ApiClientException(response.status(), clientErrorResponse);
    }
}
