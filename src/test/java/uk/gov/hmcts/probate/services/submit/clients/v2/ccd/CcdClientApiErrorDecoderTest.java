package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import feign.Request;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static feign.Util.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class CcdClientApiErrorDecoderTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Map<String, Collection<String>> headers = new LinkedHashMap<>();

    private ErrorDecoder errorDecoder = new CcdClientApiErrorDecoder();

    @Test
    public void throwsCcdClientApiException() throws Throwable {
        thrown.expect(CcdClientApiException.class);

        Response response = Response.builder()
                .status(500)
                .reason("Internal server error")
                .request(Request.create(HttpMethod.GET.toString(), "/api", Collections.emptyMap(), null, Util.UTF_8))
                .headers(headers)
                .build();

        throw errorDecoder.decode("Service#foo()", response);
    }

    @Test
    public void throwsCcdClientApiExceptionWhenStatusIs400() throws Throwable {
        thrown.expect(CcdClientApiException.class);

        Response response = Response.builder()
                .status(400)
                .reason("Bad Request")
                .request(Request.create(HttpMethod.GET.toString(), "/api", Collections.emptyMap(), null, Util.UTF_8))
                .headers(headers)
                .body("hello world", UTF_8)
                .build();

        throw errorDecoder.decode("Service#foo()", response);
    }

    @Test
    public void throwsCcdClientApiExceptionWhenStatusIs599() throws Throwable {

        Response response = Response.builder()
                .status(599)
                .reason("Internal server error")
                .request(Request.create(HttpMethod.GET.toString(), "/api", Collections.emptyMap(), null, Util.UTF_8))
                .headers(headers)
                .body("hello world", UTF_8)
                .build();

        try {
            throw errorDecoder.decode("Service#foo()", response);
        } catch (CcdClientApiException e) {
            assertThat(e.getMessage()).isEqualTo("hello world");
            assertThat(e.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Test
    public void throwsCcdClientApiExceptionIncludesBody() throws Throwable {

        Response response = Response.builder()
                .status(500)
                .reason("Internal server error")
                .request(Request.create(HttpMethod.POST.toString(), "/api", Collections.emptyMap(), null, Util.UTF_8))
                .headers(headers)
                .body("hello world", UTF_8)
                .build();

        try {
            throw errorDecoder.decode("Service#foo()", response);
        } catch (CcdClientApiException e) {
            assertThat(e.getMessage()).isEqualTo("hello world");
            assertThat(e.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Test
    public void getHttpStatusFromResponseShouldReturn400() {
        Response response = Response.builder()
                .status(400)
                .reason("Bad Request")
                .request(Request.create(HttpMethod.POST.toString(), "/api", Collections.emptyMap(), null, Util.UTF_8))
                .headers(headers)
                .body("hello world", UTF_8)
                .build();

        CcdClientApiErrorDecoder errorDecoder = new CcdClientApiErrorDecoder();
        HttpStatus responseStatus = errorDecoder.getHttpStatusFromResponse(response);

        assertThat(responseStatus).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void getHttpStatusFromResponseShouldReturn500() {
        Response response = Response.builder()
                .status(500)
                .reason("Internal server error")
                .request(Request.create(HttpMethod.POST.toString(), "/api", Collections.emptyMap(), null, Util.UTF_8))
                .headers(headers)
                .body("hello world", UTF_8)
                .build();

        CcdClientApiErrorDecoder errorDecoder = new CcdClientApiErrorDecoder();
        HttpStatus responseStatus = errorDecoder.getHttpStatusFromResponse(response);

        assertThat(responseStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void getHttpStatusFromResponseShouldReturn500IfStatusIsUnprocessable() {
        Response response = Response.builder()
                .status(599)
                .reason("Internal server error")
                .request(Request.create(HttpMethod.POST.toString(), "/api", Collections.emptyMap(), null, Util.UTF_8))
                .headers(headers)
                .body("hello world", UTF_8)
                .build();

        CcdClientApiErrorDecoder errorDecoder = new CcdClientApiErrorDecoder();
        HttpStatus responseStatus = errorDecoder.getHttpStatusFromResponse(response);

        assertThat(responseStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
