package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.probate.model.client.CcdClientApiError;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class CcdClientApiExceptionHandlerTest {

    private CcdClientApiExceptionHandler exceptionHandler = new CcdClientApiExceptionHandler();
    private CcdClientApiError clientApiError;

    @Before
    private void setUp() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"exception\":\"uk.gov.hmcts.ccd.endpoint.exceptions.ResourceNotFoundException\",\"timestamp\":\"2019-03-18T12:42:22.384\",\"status\":404,\"error\":\"Not Found\",\"message\":\"No field found\",\"path\":\"/citizens/36/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases\",\"details\":null,\"callbackErrors\":null,\"callbackWarnings\":null}";
        clientApiError = mapper.readValue(json,CcdClientApiError.class);
    }

    @Test
    void handleCcdClientApiExceptionReturnsResponseStatus500(){
        ResponseEntity responseEntity = exceptionHandler
                .handleCcdClientApiException(new CcdClientApiException(clientApiError, HttpStatus.INTERNAL_SERVER_ERROR));

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(500);
    }

    @Test
    void handleCcdClientApiExceptionReturnsResponseStatus400(){

        ResponseEntity responseEntity = exceptionHandler
                .handleCcdClientApiException(new CcdClientApiException(clientApiError, HttpStatus.BAD_REQUEST));

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    void handleCcdClientApiExceptionWithBlankMessage(){

        ResponseEntity responseEntity = exceptionHandler
                .handleCcdClientApiException(new CcdClientApiException(clientApiError, HttpStatus.BAD_REQUEST));

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(400);
    }
}