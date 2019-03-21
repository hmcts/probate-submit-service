package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.probate.model.client.ApiClientErrorResponse;
import uk.gov.hmcts.reform.probate.model.client.ApiClientException;

import java.io.IOException;

@Slf4j
public class CcdClientApiErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Response status: {} - {}", response.status(), response.reason());

        String responseBody = responseBodyToString(response);
        ApiClientErrorResponse errorResponse = mapApiClientErrorResponse(responseBody);

        throw new ApiClientException(response.status(), errorResponse);
    }

    ApiClientErrorResponse mapApiClientErrorResponse(String body) {

        ObjectMapper mapper = new ObjectMapper();
        ApiClientErrorResponse errorResponse = new ApiClientErrorResponse();
        try {
            errorResponse = mapper.readValue(body, ApiClientErrorResponse.class);
        } catch (IOException e) {
            log.debug("Response contained empty body");
        }
        return errorResponse;
    }

    String responseBodyToString(Response response) {
        String apiError = "";
        try {
            if (response.body() != null) {
                apiError = Util.toString(response.body().asReader());
            }
        } catch (IOException ignored) {
            log.debug("Unable to read response body");
        }
        return apiError;
    }
}
