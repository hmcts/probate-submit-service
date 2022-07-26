package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import feign.Request;
import feign.Response;
import feign.Util;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.probate.model.client.ApiClientError;
import uk.gov.hmcts.reform.probate.model.client.ApiClientErrorResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static feign.Util.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
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

        assertNull(response.body());
        assertEquals("", body);
    }

    @Test
    void bodyToStringShouldReturnEmptyStringIfResponseBodyIsNotNull() {
        Response response = Response.builder()
                .status(400)
                .reason("Bad Request")
                .request(Request.create(HttpMethod.GET.toString(), "/api", Collections.emptyMap(), null, Util.UTF_8))
                .headers(headers)
                .body(new InputStream() {
                    @Override
                    public int read() throws IOException {
                        throw new IOException();
                    }
                }, 1)
                .build();

        ResponseDecorator responseDecorator = new ResponseDecorator(response);
        String body = responseDecorator.bodyToString();

        assertEquals("", body);
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
        assertEquals(500, apiClientError.getStatus());
        assertEquals("Not Found", apiClientError.getError());
        assertEquals("ResourceNotFound", apiClientError.getException());
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

        assertNull(apiClientError.getStatus());
        assertNull(apiClientError.getError());
        assertNull(apiClientError.getException());
    }
}
