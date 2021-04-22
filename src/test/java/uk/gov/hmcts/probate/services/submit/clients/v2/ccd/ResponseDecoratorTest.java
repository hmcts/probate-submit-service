package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import feign.Request;
import feign.Response;
import feign.Util;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import uk.gov.hmcts.reform.probate.model.client.ApiClientError;
import uk.gov.hmcts.reform.probate.model.client.ApiClientErrorResponse;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static feign.Util.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ResponseDecoratorTest {

    private Map<String, Collection<String>> headers = new LinkedHashMap<>();

    @Test
    public void bodyToStringShouldReturnString() {
        Response response = Response.builder()
            .status(400)
            .reason("Bad Request")
            .request(Request.create(HttpMethod.GET.toString(), "/api", Collections.emptyMap(), null, Util.UTF_8))
            .headers(headers)
            .body("hello world", UTF_8)
            .build();

        ResponseDecorator responseDecorator = new ResponseDecorator(response);
        String body = responseDecorator.bodyToString();

        assertThat(body).isEqualTo("hello world");
    }

    @Test
    public void bodyToStringShouldReturnEmptyStringIfResponseBodyIsNull() {
        Response response = Response.builder()
            .status(400)
            .reason("Bad Request")
            .request(Request.create(HttpMethod.GET.toString(), "/api", Collections.emptyMap(), null, Util.UTF_8))
            .headers(headers)
            .build();

        ResponseDecorator responseDecorator = new ResponseDecorator(response);
        String body = responseDecorator.bodyToString();

        assertThat(response.body()).isNull();
        assertThat(body).isEqualTo("");
    }

    @Test
    public void mapBodyToApiClientErrorShouldReturnApiClientError() {
        String validApiClientErrorResponse =
            "{\"status\":500,\"error\":\"Not Found\",\"exception\":\"ResourceNotFound\"}";

        Response response = Response.builder()
            .status(500)
            .reason("Bad Request")
            .request(Request.create(HttpMethod.GET.toString(), "/api", Collections.emptyMap(), null, Util.UTF_8))
            .headers(headers)
            .body(validApiClientErrorResponse, UTF_8)
            .build();

        ResponseDecorator responseDecorator = new ResponseDecorator(response);
        ApiClientErrorResponse errorResponse = (ApiClientErrorResponse) responseDecorator.mapBodyToErrorResponse();

        ApiClientError apiClientError = errorResponse.getError();
        assertThat(apiClientError.getStatus()).isEqualTo(500);
        assertThat(apiClientError.getError()).isEqualTo("Not Found");
        assertThat(apiClientError.getException()).isEqualTo("ResourceNotFound");
    }

    @Test
    public void mapBodyToApiClientErrorShouldReturnEmptyApiClientErrorWhenResponseBodyIsNull() {

        Response response = Response.builder()
            .status(500)
            .reason("Bad Request")
            .request(Request.create(HttpMethod.GET.toString(), "/api", Collections.emptyMap(), null, Util.UTF_8))
            .headers(headers)
            .build();

        ResponseDecorator responseDecorator = new ResponseDecorator(response);
        ApiClientErrorResponse errorResponse = (ApiClientErrorResponse) responseDecorator.mapBodyToErrorResponse();
        ApiClientError apiClientError = errorResponse.getError();

        assertThat(apiClientError.getStatus()).isNull();
        assertThat(apiClientError.getError()).isNull();
        assertThat(apiClientError.getException()).isNull();
    }
}
