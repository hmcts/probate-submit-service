package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import feign.Util;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.probate.model.client.ApiClientException;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class CcdClientApiErrorDecoderTest {

    private Map<String, Collection<String>> headers = new LinkedHashMap<>();

    @Mock
    private CcdClientApiErrorDecoder errorDecoder;


    @Test
    public void initializesObjectMapperWhenProvided() {
        ObjectMapper objectMapper = new ObjectMapper();
        CcdClientApiErrorDecoder errorDecoder = new CcdClientApiErrorDecoder(objectMapper);

        assertNotNull(errorDecoder);
    }

    @Test
    public void initializesWithoutObjectMapperWhenNotProvided() {
        CcdClientApiErrorDecoder errorDecoder = new CcdClientApiErrorDecoder();

        assertNotNull(errorDecoder);
    }

    @Test
    public void throwsApiClientExceptionWhenResponseIs500() throws ReflectiveOperationException  {
        Response response = Response.builder()
                .status(500)
                .reason("Internal Server Error")
                .request(Request.create(HttpMethod.GET.toString(), "/api", Collections.emptyMap(), null, Util.UTF_8))
                .headers(headers)
                .build();

        ApiClientException apiClientException = new ApiClientException(500, null);

        when(errorDecoder.decode("Service#foo()", response)).thenThrow(apiClientException);

        assertThrows(ApiClientException.class, () -> errorDecoder.decode("Service#foo()", response));
        assertEquals(500, apiClientException.getStatus());
        assertNull(apiClientException.getErrorResponse());
    }


}
