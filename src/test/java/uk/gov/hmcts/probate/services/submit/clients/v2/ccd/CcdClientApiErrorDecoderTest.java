package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import feign.Request;
import feign.Response;
import feign.Util;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.probate.model.client.ApiClientException;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
public class CcdClientApiErrorDecoderTest {

    private Map<String, Collection<String>> headers = new LinkedHashMap<>();

    private CcdClientApiErrorDecoder errorDecoder = new CcdClientApiErrorDecoder();

    @Test
    public void throwsApiClientException() throws Throwable {
        Response response = Response.builder()
            .status(500)
            .reason("Internal server error")
            .request(Request.create(HttpMethod.GET.toString(), "/api", Collections.emptyMap(), null, Util.UTF_8))
            .headers(headers)
            .build();

        assertThrows(ApiClientException.class, () -> {
            throw errorDecoder.decode("Service#foo()", response);
        });
    }

}
