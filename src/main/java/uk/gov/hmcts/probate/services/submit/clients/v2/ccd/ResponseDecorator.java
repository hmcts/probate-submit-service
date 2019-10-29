package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;


import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.probate.model.client.ApiClientError;
import uk.gov.hmcts.reform.probate.model.client.ApiClientErrorResponse;
import uk.gov.hmcts.reform.probate.model.client.ErrorResponse;

import java.io.IOException;

@Slf4j
class ResponseDecorator {

    private Response response;

    ResponseDecorator(Response response) {
        this.response=response;
    }

    String bodyToString() {
        String apiError = "";
        try {
            if (this.response.body() != null) {
                apiError = Util.toString(this.response.body().asReader());
            }
        } catch (IOException ignored) {
            log.debug("Unable to read response body");
        }
        return apiError;
    }

    ErrorResponse mapBodyToErrorResponse() {
        ObjectMapper mapper = new ObjectMapper();

        ApiClientError clientError = new ApiClientError();
        try {
            clientError = mapper.readValue(this.bodyToString(), ApiClientError.class);
        } catch (IOException e) {
            log.debug("Response contained empty body");
        }
        return new ApiClientErrorResponse(clientError);
    }
}
