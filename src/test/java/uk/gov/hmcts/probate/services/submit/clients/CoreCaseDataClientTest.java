package uk.gov.hmcts.probate.services.submit.clients;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import java.util.Calendar;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;

@RunWith(MockitoJUnitRunner.class)
public class CoreCaseDataClientTest {

    public static final String PRIMARY_APPLICANT_EMAIL_ADDRESS = "primaryApplicantEmailAddress";
    public static final String DECEASED_SURNAME = "deceasedSurname";
    public static final String DECEASED_FORENAMES = "deceasedForenames";
    public static final String DECEASED_DATE_OF_DEATH = "deceasedDateOfDeath";
    public static final String CASE_PREFIX = "case.";

    @Mock
    private RequestFactory entityBuilder;

    @Mock
    private RestTemplate restTemplate;
    
    @Mock 
    private CoreCaseDataMapper ccdDataMapper;
    
    @InjectMocks
    private CoreCaseDataClient coreCaseDataClient;

    @Captor
    private ArgumentCaptor<Map<String, String>> argumentCaptorMap;

    private String userId;
    private String authorizationToken;
    private JsonNode ccdStartCaseResponse;
    private Calendar submissionTimestamp;
    private JsonNode sequenceNumber;
    private ObjectMapper objectMapper;

    private static final String CORE_CASE_DATA_URL =
        "http://localhost:4452/citizens/%s/jurisdictions/PROBATE/case-types/GrantOfRepresentation";


    @Before
    public void setUp() throws Exception {
        ReflectionTestUtils.setField(coreCaseDataClient, "coreCaseDataServiceURL", CORE_CASE_DATA_URL);
        userId = "123";
        authorizationToken = "dummyToken";
        ccdStartCaseResponse = TestUtils.getJsonNodeFromFile("ccdStartCaseResponse.json");

        submissionTimestamp = Calendar.getInstance();
        sequenceNumber = new LongNode(123L);
        objectMapper = new ObjectMapper();
    }

    @Test
    public void shouldSaveCase() throws IOException {
        String expectedURL = String.format(CORE_CASE_DATA_URL, userId) + "/cases";
        HttpEntity<JsonNode> persistenceReq = new HttpEntity<>(new TextNode("requestBody"), new HttpHeaders());
        when(entityBuilder.createCcdSaveRequest(any(),any())).thenReturn(persistenceReq);

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        ResponseEntity<JsonNode> mockResponse = new ResponseEntity<>(new TextNode("responseBody"), HttpStatus.CREATED);
        doReturn(mockResponse).when(restTemplate).exchange(argumentCaptor.capture(), eq(HttpMethod.POST), isA(HttpEntity.class), eq(JsonNode.class));

        JsonNode mappedData =  TestUtils.getJsonNodeFromFile("mappedData.json");
        doReturn(mappedData).when(ccdDataMapper).createCcdData(any(), any(), any(),any(),any());

        
        coreCaseDataClient.saveCase(persistenceReq.getBody(), userId, authorizationToken, ccdStartCaseResponse,
            submissionTimestamp, sequenceNumber);

        assertThat(argumentCaptor.getValue(), is(equalTo(expectedURL)));
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), isA(HttpEntity.class), eq(JsonNode.class));
    }

//    @Test
//    public void shouldGetCase() throws IOException {
//        String expectedURL = String.format(CORE_CASE_DATA_URL, userId) + "/cases";
//        String email = "test@test.com";
//        String deceasedSurname = "Joe";
//        String deceasedForenames = "Bloggs";
//        String deceasedDateOfDeath = "1/8/2018";
//
//        ObjectNode objectNode = objectMapper.createObjectNode();
//        objectNode.put(PRIMARY_APPLICANT_EMAIL_ADDRESS, email);
//        objectNode.put(DECEASED_SURNAME, deceasedSurname);
//        objectNode.put(DECEASED_FORENAMES, deceasedForenames);
//        objectNode.put(DECEASED_DATE_OF_DEATH, deceasedDateOfDeath);
//        HttpEntity<JsonNode> persistenceReq = new HttpEntity<>(objectNode, new HttpHeaders());
//        when(entityBuilder.createCcdSaveRequest(any(),any())).thenReturn(persistenceReq);
//
//        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
//        ResponseEntity<JsonNode> mockResponse = new ResponseEntity<>(objectNode, HttpStatus.CREATED);
//        doReturn(mockResponse).when(restTemplate).exchange(anyString(), eq(HttpMethod.GET), isA(HttpEntity.class), eq(JsonNode.class), anyMap());
//
//        JsonNode mappedData =  TestUtils.getJsonNodeFromFile("mappedData.json");
//        doReturn(mappedData).when(ccdDataMapper).createCcdData(any(), any(), any(),any(),any());
//
//        coreCaseDataClient.getCase(persistenceReq.getBody(), userId, authorizationToken);
//
//        assertThat(argumentCaptor.getValue(), is(equalTo(expectedURL)));
//        assertThat(argumentCaptorMap.getValue().get(CASE_PREFIX + PRIMARY_APPLICANT_EMAIL_ADDRESS), is(equalTo(email)));
//        assertThat(argumentCaptorMap.getValue().get(CASE_PREFIX + DECEASED_SURNAME), is(equalTo(deceasedSurname)));
//        assertThat(argumentCaptorMap.getValue().get(CASE_PREFIX + DECEASED_FORENAMES), is(equalTo(deceasedForenames)));
//        assertThat(argumentCaptorMap.getValue().get(CASE_PREFIX + DECEASED_DATE_OF_DEATH), is(equalTo(deceasedDateOfDeath)));
//        verify(restTemplate, times(1)).exchange(eq(expectedURL), eq(HttpMethod.GET), isA(HttpEntity.class), eq(JsonNode.class), anyMap());
//    }

    @Test(expected = RestClientException.class)
    public void processFailTest() throws IOException {
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        doThrow(RestClientException.class).when(restTemplate).exchange(argumentCaptor.capture(), eq(HttpMethod.POST), isNull(HttpEntity.class), eq(JsonNode.class));
       
        JsonNode mappedData =  TestUtils.getJsonNodeFromFile("mappedData.json");
        doReturn(mappedData).when(ccdDataMapper).createCcdData(any(), any(), any(),any(),any());
        
        coreCaseDataClient.saveCase(NullNode.getInstance(), userId, authorizationToken, ccdStartCaseResponse,
            submissionTimestamp, sequenceNumber);

        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), isA(HttpEntity.class), eq(JsonNode.class));
    }
}
