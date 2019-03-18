package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.probate.model.client.CcdClientApiError;

import static feign.FeignException.errorStatus;

@Slf4j
public class CcdClientApiErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {

        log.error("Response status: {} - {}", response.status(), response.reason());

        HttpStatus responseStatus = getHttpStatusFromResponse(response);
        CcdClientApiError ccdClientApiError = mapBodyToCcdApiError(response.body());

        if(responseStatus.is5xxServerError() || responseStatus.is4xxClientError()){
            throw new CcdClientApiException(ccdClientApiError, responseStatus);
        }

        return errorStatus(methodKey, response);
    }

    CcdClientApiError mapBodyToCcdApiError(Response.Body body){
        ObjectMapper mapper = new ObjectMapper();
        CcdClientApiError clientApiError;
        try {
            clientApiError = mapper.readValue(body.toString(),CcdClientApiError.class);
        } catch (Exception e) {
            log.debug("CcdClientApi response contained empty body");
            clientApiError = new CcdClientApiError();
        }
        return clientApiError;
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
