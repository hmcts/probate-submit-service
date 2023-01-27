package uk.gov.hmcts.probate.services.submit.controllers.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.probate.model.client.ApiClientError;
import uk.gov.hmcts.reform.probate.model.client.ApiClientErrorResponse;
import uk.gov.hmcts.reform.probate.model.client.ApiClientException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class ProbateExceptionHandlerIT {

    private ProbateExceptionHandler exceptionHandler = new ProbateExceptionHandler();
    private ApiClientErrorResponse clientErrorResponse;

    @BeforeEach
    public void setUp() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json =
            "{\"exception\":\"uk.gov.hmcts.ccd.endpoint.exceptions.ResourceNotFoundException\","
                + "\"timestamp\":\"2019-03-18T12:42:22.384\",\"error\":\"Not Found\",\"message\":\"No field found\","
                + "\"path\":\"/citizens/36/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases\","
                + "\"details\":null,\"callbackErrors\":null,\"callbackWarnings\":null}";
        ApiClientError apiClientError = mapper.readValue(json, ApiClientError.class);
        clientErrorResponse = new ApiClientErrorResponse(apiClientError);
    }

    @Test
    public void handleApiClientExceptionReturnsResponseStatus500FromException() {
        ResponseEntity responseEntity = exceptionHandler
            .handleApiClientException(new ApiClientException(500, clientErrorResponse));

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(500);
    }

    @Test
    public void handleApiClientExceptionReturnsResponseStatus400FromException() {
        ResponseEntity responseEntity = exceptionHandler
            .handleApiClientException(new ApiClientException(400, clientErrorResponse));

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    public void handleApiClientExceptionReturns500IfResponseStatusIsUnprocessable() {
        ResponseEntity responseEntity = exceptionHandler
            .handleApiClientException(new ApiClientException(575, clientErrorResponse));

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(500);
    }

}
