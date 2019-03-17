package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import static feign.FeignException.errorStatus;

@Slf4j
public class CcdClientApiErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {

        HttpStatus responseStatus = getHttpStatusFromResponse(response);
        String responseBody = response.body() == null ? "" : response.body().toString();

        log.error("CcdClientApi responded with {} - {}", responseStatus.value(), responseBody);

        if(responseStatus.is5xxServerError() || responseStatus.is4xxClientError()){
            throw new CcdClientApiException(responseBody, responseStatus);
        }

        return errorStatus(methodKey, response);
    }

    HttpStatus getHttpStatusFromResponse(Response response){
        HttpStatus responseStatus;
        try{
            responseStatus = HttpStatus.valueOf(response.status());
        } catch (IllegalArgumentException ex) {
            log.debug("CcdClientApi responded with unprocessable HttpStatus");
            responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return responseStatus;
    }

}
