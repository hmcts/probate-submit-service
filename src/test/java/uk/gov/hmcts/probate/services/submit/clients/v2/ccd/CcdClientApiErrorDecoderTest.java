package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import feign.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import uk.gov.hmcts.reform.probate.model.client.ApiClientException;

import java.util.Collections;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@ExtendWith(MockitoExtension.class)
class CcdClientApiErrorDecoderTest {

    private CcdClientApiErrorDecoder errorDecoder;

    @BeforeEach
    void setUp() {
        errorDecoder = new CcdClientApiErrorDecoder(new ObjectMapper());
    }

    @Test
    void initializesObjectMapperWhenProvided() {
        ObjectMapper objectMapper = new ObjectMapper();
        CcdClientApiErrorDecoder errorDecoder = new CcdClientApiErrorDecoder(objectMapper);

        assertNotNull(errorDecoder);
    }

    @Test
    void initializesWithoutObjectMapperWhenNotProvided() {
        CcdClientApiErrorDecoder errorDecoder = new CcdClientApiErrorDecoder();

        assertNotNull(errorDecoder);
    }

    @Test
    void throwsApiClientExceptionWhenResponseIs500() {
        Response response = Response.builder()
                .status(500)
                .reason("Internal Server Error")
                .request(Request.create(HttpMethod.GET.toString(), "/api", Collections.emptyMap(), null, Util.UTF_8))
                .headers(Collections.emptyMap())
                .build();

        ApiClientException exception = assertInstanceOf(
                ApiClientException.class,
                errorDecoder.decode("Service#foo()", response));

        assertEquals(500, exception.getStatus());
        assertNotNull(exception.getErrorResponse());
    }


}
