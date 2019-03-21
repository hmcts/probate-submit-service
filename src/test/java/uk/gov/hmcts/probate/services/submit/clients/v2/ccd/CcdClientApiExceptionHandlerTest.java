package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.probate.model.client.ApiClientErrorResponse;
import uk.gov.hmcts.reform.probate.model.client.ApiClientException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CcdClientApiExceptionHandlerTest {

    private CcdClientApiExceptionHandler exceptionHandler = new CcdClientApiExceptionHandler();
    private ApiClientErrorResponse clientApiError;

    @Before
    public void setUp() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"exception\":\"uk.gov.hmcts.ccd.endpoint.exceptions.ResourceNotFoundException\",\"timestamp\":\"2019-03-18T12:42:22.384\",\"status\":404,\"error\":\"Not Found\",\"message\":\"No field found\",\"path\":\"/citizens/36/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases\",\"details\":null,\"callbackErrors\":null,\"callbackWarnings\":null}";
        clientApiError = mapper.readValue(json, ApiClientErrorResponse.class);
    }

    @Test
    public void handleCcdClientApiExceptionReturnsResponseStatus500(){
        ResponseEntity responseEntity = exceptionHandler
                .handleApiClientException(new ApiClientException(500, clientApiError));

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(500);
    }

    @Test
    public void handleCcdClientApiExceptionReturnsResponseStatus400(){

        ResponseEntity responseEntity = exceptionHandler
                .handleApiClientException(new ApiClientException(400, clientApiError));

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    public void handleCcdClientApiExceptionWithBlankMessage(){

        ResponseEntity responseEntity = exceptionHandler
                .handleApiClientException(new ApiClientException(400, clientApiError));

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(400);
    }
}