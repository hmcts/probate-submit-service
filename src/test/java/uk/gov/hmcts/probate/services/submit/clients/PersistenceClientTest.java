package uk.gov.hmcts.probate.services.submit.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.probate.services.submit.model.PersistenceResponse;
import uk.gov.hmcts.probate.services.submit.model.SubmitData;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.endsWith;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PersistenceClientTest {

    private static String REGISTRY_URL = "http://localhost:8181/registry";
    private static String SUBMIT_URL = "http://localhost:8181/submit";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private RequestFactory requestFactory;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PersistenceClient persistenceClient;

    private TestUtils testUtils;
    private JsonNode registryData;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        persistenceClient = new PersistenceClient(restTemplate, requestFactory);
        ReflectionTestUtils.setField(persistenceClient, "registryPersistenceUrl", REGISTRY_URL);
        ReflectionTestUtils.setField(persistenceClient, "submissionsPersistenceUrl", SUBMIT_URL);
    }

    @Test
    public void saveSubmissionSuccessTest() throws IOException {
        HttpEntity<JsonNode> persistenceReq = new HttpEntity<>(new TextNode("requestBody"), new HttpHeaders());
        when(requestFactory.createPersistenceRequest(eq(persistenceReq.getBody()))).thenReturn(persistenceReq);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree("{\"id\": 1234 }");
        ResponseEntity<JsonNode> mockResponse = new ResponseEntity<>(jsonNode, HttpStatus.CREATED);
        doReturn(mockResponse).when(restTemplate).postForEntity(anyString(), eq(persistenceReq), eq(JsonNode.class));

        PersistenceResponse actualResponse = persistenceClient.saveSubmission(new SubmitData(persistenceReq.getBody()));

        verify(restTemplate, times(1)).postForEntity(anyString(), eq(persistenceReq), eq(JsonNode.class));
        assertEquals(actualResponse.getIdAsLong().intValue(), mockResponse.getBody().get("id").intValue());
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
        when(requestFactory.createPersistenceRequest(any())).thenReturn(persistenceReq);

        persistenceClient.updateFormData("emailId", Long.parseLong("123456789"), new TextNode("requestBody"));

        verify(restTemplate, times(1)).put(endsWith("/emailId"), eq(persistenceReq));
    }

    @Test
    public void loadFormDataByIdSuccessTest() {
        ResponseEntity<JsonNode> mockResponse = new ResponseEntity<>(new TextNode("response"), HttpStatus.CREATED);
        doReturn(mockResponse).when(restTemplate).getForEntity(endsWith("/emailId"), eq(JsonNode.class));

        JsonNode actualResponse = persistenceClient.loadFormDataById("emailId").getJson();

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

        persistenceClient.saveSubmission(new SubmitData(NullNode.getInstance()));

        verify(restTemplate, times(1)).postForEntity(anyString(), any(), any());
    }

    @Test
    public void getNextSequenceNumber(){
        ResponseEntity<JsonNode> mockResponse = new ResponseEntity<>(new LongNode(1234L), HttpStatus.CREATED);
        doReturn(mockResponse).when(restTemplate).getForEntity(endsWith("/1234"), eq(JsonNode.class));

        JsonNode result = persistenceClient.getNextRegistry(1234L);
        verify(restTemplate, times(1)).getForEntity(endsWith("/1234"), eq(JsonNode.class));
        assertEquals(result, mockResponse.getBody());
    }
}