package uk.gov.hmcts.probate.unit;

import feign.Request;
import feign.Response;
import feign.Util;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import uk.gov.hmcts.probate.services.submit.clients.v2.ccd.CcdClientApiErrorDecoder;
import uk.gov.hmcts.reform.probate.model.client.ApiClientErrorResponse;
import uk.gov.hmcts.reform.probate.model.client.ApiClientException;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static feign.Util.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CcdClientApiErrorDecoderTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Map<String, Collection<String>> headers = new LinkedHashMap<>();

    private CcdClientApiErrorDecoder errorDecoder = new CcdClientApiErrorDecoder();

    @Test
    public void throwsApiClientException() throws Throwable {
        thrown.expect(ApiClientException.class);

        Response response = Response.builder()
                .status(500)
                .reason("Internal server error")
                .request(Request.create(HttpMethod.GET.toString(), "/api", Collections.emptyMap(), null, Util.UTF_8))
                .headers(headers)
                .build();

        throw errorDecoder.decode("Service#foo()", response);
    }

}
