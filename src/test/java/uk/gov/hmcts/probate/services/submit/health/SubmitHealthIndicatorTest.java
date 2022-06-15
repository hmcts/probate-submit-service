package uk.gov.hmcts.probate.services.submit.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class SubmitHealthIndicatorTest {

    private static final String URL = "http://url.com";

    @Mock
    private RestTemplate mockRestTemplate;

    @Mock
    private ResponseEntity<String> mockResponseEntity;

    private SubmitHealthIndicator submitHealthIndicator;

    @BeforeEach
    public void setUp() {
        submitHealthIndicator = new SubmitHealthIndicator(URL, mockRestTemplate);
    }

    @Test
    public void shouldReturnStatusOfUpWhenHttpStatusIsOK() {
        when(mockRestTemplate.getForEntity(URL + "/health", String.class)).thenReturn(mockResponseEntity);
        when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        Health health = submitHealthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals(URL, health.getDetails().get("url"));
    }

    @Test
    public void shouldReturnStatusOfDownWhenHttpStatusIsNotOK() {
        when(mockRestTemplate.getForEntity(URL + "/health", String.class)).thenReturn(mockResponseEntity);
        when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.NO_CONTENT);
        when(mockResponseEntity.getStatusCodeValue()).thenReturn(HttpStatus.NO_CONTENT.value());
        Health health = submitHealthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals(URL, health.getDetails().get("url"));
        assertEquals("HTTP Status code not 200", health.getDetails().get("message"));
        assertEquals("HTTP Status: 204", health.getDetails().get("exception"));
    }

    @Test
    public void shouldReturnStatusOfDownWhenResourceAccessExceptionIsThrown() {
        final String message = "EXCEPTION MESSAGE";
        when(mockRestTemplate.getForEntity(URL + "/health", String.class))
            .thenThrow(new ResourceAccessException(message));

        Health health = submitHealthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals(URL, health.getDetails().get("url"));
        assertEquals(message, health.getDetails().get("message"));
        assertEquals("ResourceAccessException", health.getDetails().get("exception"));
    }

    @Test
    public void shouldReturnStatusOfDownWhenHttpStatusCodeExceptionIsThrown() {
        when(mockRestTemplate.getForEntity(URL + "/health", String.class))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        Health health = submitHealthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals(URL, health.getDetails().get("url"));
        assertEquals("400 BAD_REQUEST", health.getDetails().get("message"));
        assertEquals("HttpStatusCodeException - HTTP Status: 400", health.getDetails().get("exception"));
    }

    @Test
    public void shouldReturnStatusOfDownWhenUnknownHttpStatusCodeExceptionIsThrown() {
        final String statusText = "status text";
        when(mockRestTemplate.getForEntity(URL + "/health", String.class))
            .thenThrow(new UnknownHttpStatusCodeException(1000, statusText, null, null, null));

        Health health = submitHealthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals(URL, health.getDetails().get("url"));
        assertEquals("Unknown status code [1000] status text", health.getDetails().get("message"));
        assertEquals("UnknownHttpStatusCodeException - " + statusText, health.getDetails().get("exception"));
    }
}
