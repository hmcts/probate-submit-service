package uk.gov.hmcts.probate.services.submit.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.probate.services.submit.model.FormData;
import static org.mockito.Matchers.anyString;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;


public class PersistenceClientTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PersistenceClient persistenceClient;

    @Test
    public void loadFormDataByIdSuccessTest() {
        ResponseEntity<JsonNode> mockResponse = new ResponseEntity<>(new TextNode("response"), HttpStatus.CREATED);
        doReturn(mockResponse).when(restTemplate).getForEntity(endsWith("/emailId"), eq(JsonNode.class));

        FormData actualResponse = persistenceClient.loadFormDataById("emailId");

        verify(restTemplate, times(1)).getForEntity(endsWith("/emailId"), eq(JsonNode.class));
        assertEquals(actualResponse.getJson(), mockResponse.getBody());
    }
}