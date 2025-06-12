package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import feign.Util;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.probate.model.client.ApiClientException;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class CcdClientApiErrorDecoderTest {

    private Map<String, Collection<String>> headers = new LinkedHashMap<>();

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void throwsApiClientException() throws Throwable {
        Response response = Response.builder()
            .status(500)
            .reason("Internal server error")
            .request(Request.create(HttpMethod.GET.toString(), "/api", Collections.emptyMap(), null, Util.UTF_8))
            .headers(headers)
            .build();
        ResponseDecorator responseDecorator = new ResponseDecorator(response);

        Field objectMapperField = ResponseDecorator.class.getDeclaredField("objectMapper");
        objectMapperField.setAccessible(true);
        objectMapperField.set(responseDecorator, objectMapper);

        CcdClientApiErrorDecoder errorDecoder = new CcdClientApiErrorDecoder(responseDecorator);

        assertThrows(ApiClientException.class, () -> {
            throw errorDecoder.decode("Service#foo()", response);
        });
    }

}
