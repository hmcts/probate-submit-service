package uk.gov.hmcts.probate.services.submit.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class PersistenceClientTest {

    @Mock
    private RequestFactory entityBuilder;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PersistenceClient persistenceClient;

    @Test
    public void saveSubmissionSuccessTest() {
        HttpEntity<JsonNode> persistenceReq = new HttpEntity<>(new TextNode("requestBody"), new HttpHeaders());
        when(entityBuilder.createPersistenceRequest(eq(persistenceReq.getBody()))).thenReturn(persistenceReq);

        ResponseEntity<JsonNode> mockResponse = new ResponseEntity<>(new TextNode("responseBody"), HttpStatus.CREATED);
        doReturn(mockResponse).when(restTemplate).postForEntity(anyString(), eq(persistenceReq), eq(JsonNode.class));

        JsonNode actualResponse = persistenceClient.saveSubmission(persistenceReq.getBody());

        verify(restTemplate, times(1)).postForEntity(anyString(), eq(persistenceReq), eq(JsonNode.class));
        assertEquals(actualResponse, mockResponse.getBody());
    }

    @Test
    public void loadSubmissionSuccessTest() {
        ResponseEntity<JsonNode> mockResponse = new ResponseEntity<>(new TextNode("response"), HttpStatus.CREATED);
        doReturn(mockResponse).when(restTemplate).getForEntity(anyString(), eq(JsonNode.class));

        JsonNode actualResponse = persistenceClient.loadSubmission(Long.parseLong("123456789"));

        verify(restTemplate, times(1)).getForEntity(endsWith("/123456789"), eq(JsonNode.class));
        assertEquals(actualResponse, mockResponse.getBody());
    }

    @Test
    public void updateFormDataSuccessTest() {
        HttpEntity<JsonNode> persistenceReq = new HttpEntity<>(new TextNode("requestBody"), new HttpHeaders());
        when(entityBuilder.createPersistenceRequest(any())).thenReturn(persistenceReq);

        persistenceClient.updateFormData("emailId", Long.parseLong("123456789"), new TextNode("requestBody"));

        verify(restTemplate, times(1)).put(endsWith("/emailId"), eq(persistenceReq));
    }

    @Test
    public void loadFormDataByIdSuccessTest() {
        ResponseEntity<JsonNode> mockResponse = new ResponseEntity<>(new TextNode("response"), HttpStatus.CREATED);
        doReturn(mockResponse).when(restTemplate).getForEntity(endsWith("/emailId"), eq(JsonNode.class));

        JsonNode actualResponse = persistenceClient.loadFormDataById("emailId");

        verify(restTemplate, times(1)).getForEntity(endsWith("/emailId"), eq(JsonNode.class));
        assertEquals(actualResponse, mockResponse.getBody());
    }

    @Test
    public void loadFormDataBySubmissionReferenceSuccessTest() {
        ResponseEntity<JsonNode> mockResponse = new ResponseEntity<>(new TextNode("response"), HttpStatus.CREATED);
        doReturn(mockResponse).when(restTemplate).getForEntity(endsWith("/search/findBySubmissionReference?submissionReference=1234"), eq(JsonNode.class));

        JsonNode actualResponse = persistenceClient.loadFormDataBySubmissionReference(1234);

        verify(restTemplate, times(1)).getForEntity(endsWith("/search/findBySubmissionReference?submissionReference=1234"), eq(JsonNode.class));
        assertEquals(actualResponse, mockResponse.getBody());
    }

    @Test(expected = RestClientException.class)
    public void processFailTest() {
        doThrow(RestClientException.class).when(restTemplate).postForEntity(anyString(), any(), any());

        persistenceClient.saveSubmission(NullNode.getInstance());

        verify(restTemplate, times(1)).postForEntity(anyString(), any(), any());
    }

    @Test
    public void getNextSequenceNumber(){
        ResponseEntity<Long> mockResponse = new ResponseEntity<>(1234l, HttpStatus.CREATED);
        doReturn(mockResponse).when(restTemplate).getForEntity(endsWith("/RegistryName"), eq(Long.class));

        Long result = persistenceClient.getNextSequenceNumber("RegistryName");
        verify(restTemplate, times(1)).getForEntity(endsWith("/RegistryName"), eq(Long.class));
        assertEquals(result, mockResponse.getBody());
    }
}