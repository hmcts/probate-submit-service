package uk.gov.hmcts.probate.services.submit.clients;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.ImmutableMap;

import java.util.Calendar;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.probate.services.submit.model.CcdCaseResponse;
import uk.gov.hmcts.probate.services.submit.model.FormData;
import uk.gov.hmcts.probate.services.submit.model.PaymentResponse;
import uk.gov.hmcts.probate.services.submit.model.SubmitData;

@RunWith(MockitoJUnitRunner.class)
public class CoreCaseDataClientTest {

    private static final String CORE_CASE_DATA_URL =
            "http://localhost:4452/citizens/%s/jurisdictions/PROBATE/case-types/GrantOfRepresentation";
    private static final String USER_ID = "12345";
    private static final Long CASE_ID = 9999999L;
    private static final String AUTHORIZATION_TOKEN = "XXXXXX";
    private static final Calendar SUBMISSION_TIMESTAMP = Calendar.getInstance();
    private static final JsonNode SEQUENCE_NUMBER = new LongNode(123L);
    private static final String APPLY_FOR_GRANT_CCD_EVENT_ID = "applyForGrant";
    private static final String CCD_EVENT_ID = "CCD_EVENT_ID";
    private static final String TOKEN_RESOURCE = "token";
    private static final String UPDATE_PAYMENT_STATUS_CCD_EVENT_ID = "createCase";

    public static final String APPLICANT_EMAIL_ADDRESS_FIELD = "applicantEmail";
    public static final String DECEASED_SURNAME_FIELD = "deceasedSurname";
    public static final String DECEASED_FORENAMES_FIELD = "deceasedFirstname";

    public static final JsonNode PRIMARY_APPLICANT_EMAIL_ADDRESS = new TextNode("test@test.com");
    public static final JsonNode DECEASED_SURNAME = new TextNode("Brown");
    public static final JsonNode DECEASED_FORENAMES = new TextNode("Bobby");

    private CcdCreateCaseParams ccdCreateCaseParams;
    private ObjectMapper objectMapper;

    @Mock
    private JsonNode registryData;

    @Mock
    private JsonNode tokenJsonNode;

    @Mock
    private HttpEntity<JsonNode> ccdRequest;

    @Mock
    private RequestFactory requestFactory;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CoreCaseDataMapper ccdDataMapper;

    @Mock
    private SubmitData submitData;

    @Mock
    private JsonNode submitDataJson;

    @Mock
    private ResponseEntity<JsonNode> response;

    @Mock
    private JsonNode ccdData;

    @Mock
    private CcdCaseResponse ccdCaseResponse;

    @Mock
    private PaymentResponse paymentResponse;

    @InjectMocks
    private CoreCaseDataClient coreCaseDataClient;

    @Before
    public void setUp() {
        ReflectionTestUtils
                .setField(coreCaseDataClient, "coreCaseDataServiceURL", CORE_CASE_DATA_URL);

        objectMapper = new ObjectMapper();

        ccdCreateCaseParams = new CcdCreateCaseParams.Builder()
                .withAuthorisation(AUTHORIZATION_TOKEN)
                .withRegistryData(registryData)
                .withSubmissionReference(SEQUENCE_NUMBER)
                .withSubmitData(submitData)
                .withUserId(USER_ID)
                .withSubmissionTimestamp(SUBMISSION_TIMESTAMP)
                .build();

        when(submitData.getSubmitData()).thenReturn(submitDataJson);
        when(submitDataJson.get(APPLICANT_EMAIL_ADDRESS_FIELD))
                .thenReturn(PRIMARY_APPLICANT_EMAIL_ADDRESS);
        when(submitDataJson.get(DECEASED_SURNAME_FIELD)).thenReturn(DECEASED_SURNAME);
        when(submitDataJson.get(DECEASED_FORENAMES_FIELD)).thenReturn(DECEASED_FORENAMES);

        when(ccdCaseResponse.getCaseId()).thenReturn(CASE_ID);
    }

    @Test
    public void shouldCreateCase() {
        String url = "http://localhost:4452/citizens/12345/jurisdictions/PROBATE/case-types/GrantOfRepresentation/" +
                "event-triggers/applyForGrant/token";
        when(restTemplate.exchange(url, HttpMethod.GET, ccdRequest, JsonNode.class))
                .thenReturn(response);
        when(requestFactory.createCcdStartRequest(ccdCreateCaseParams.getAuthorization()))
                .thenReturn(ccdRequest);
        when(response.getBody()).thenReturn(ccdData);
        when(ccdData.get(TOKEN_RESOURCE)).thenReturn(tokenJsonNode);

        JsonNode caseTokenJson = coreCaseDataClient.createCase(ccdCreateCaseParams);

        assertThat(caseTokenJson, is(notNullValue()));
        verify(restTemplate, times(1)).exchange(url, HttpMethod.GET, ccdRequest, JsonNode.class);
        verify(requestFactory, times(1)).createCcdStartRequest(ccdCreateCaseParams.getAuthorization());
    }

    @Test(expected = HttpClientErrorException.class)
    public void shouldThrowHttpClientErrorExceptionCreateCaseWhenRestTemplateException() {
        String url = "http://localhost:4452/citizens/12345/jurisdictions/PROBATE/case-types/GrantOfRepresentation/" +
                "event-triggers/applyForGrant/token";
        doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST))
                .when(restTemplate).exchange(url, HttpMethod.GET, ccdRequest, JsonNode.class);
        when(requestFactory.createCcdStartRequest(ccdCreateCaseParams.getAuthorization()))
                .thenReturn(ccdRequest);
        when(response.getBody()).thenReturn(ccdData);
        when(ccdData.get(TOKEN_RESOURCE)).thenReturn(tokenJsonNode);

        coreCaseDataClient.createCase(ccdCreateCaseParams);
    }

    @Test
    public void shouldSaveCase() {
        String url = "http://localhost:4452/citizens/12345/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases";
        when(ccdDataMapper.createCcdData(submitDataJson,
                APPLY_FOR_GRANT_CCD_EVENT_ID, tokenJsonNode, ccdCreateCaseParams.getSubmissionTimestamp(),
                ccdCreateCaseParams.getRegistryData())).thenReturn(ccdData);

        when(requestFactory.createCcdSaveRequest(ccdData, ccdCreateCaseParams.getAuthorization()))
                .thenReturn(ccdRequest);
        when(restTemplate.exchange(url, HttpMethod.POST, ccdRequest, JsonNode.class))
                .thenReturn(response);

        CcdCaseResponse ccdCaseResponse = coreCaseDataClient
                .saveCase(ccdCreateCaseParams, tokenJsonNode);

        assertThat(ccdCaseResponse, is(notNullValue()));
        verify(ccdDataMapper, times(1)).createCcdData(submitDataJson,
                APPLY_FOR_GRANT_CCD_EVENT_ID, tokenJsonNode, ccdCreateCaseParams.getSubmissionTimestamp(),
                ccdCreateCaseParams.getRegistryData());
        verify(requestFactory, times(1))
                .createCcdSaveRequest(ccdData, ccdCreateCaseParams.getAuthorization());
        verify(restTemplate, times(1)).exchange(url, HttpMethod.POST, ccdRequest, JsonNode.class);
    }

    @Test(expected = HttpClientErrorException.class)
    public void shouldThrowHttpClientErrorExceptionOnSaveCaseWhenRestTemplateException() {
        String url = "http://localhost:4452/citizens/12345/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases";
        when(ccdDataMapper.createCcdData(submitDataJson,
                APPLY_FOR_GRANT_CCD_EVENT_ID, tokenJsonNode, ccdCreateCaseParams.getSubmissionTimestamp(),
                ccdCreateCaseParams.getRegistryData())).thenReturn(ccdData);
        when(requestFactory.createCcdSaveRequest(ccdData, ccdCreateCaseParams.getAuthorization()))
                .thenReturn(ccdRequest);
        doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST))
                .when(restTemplate).exchange(url, HttpMethod.POST, ccdRequest, JsonNode.class);

        coreCaseDataClient.saveCase(ccdCreateCaseParams, tokenJsonNode);
    }

    @Test
    public void shouldGetCaseWhenExistForQueryParameters() {
        String url = "http://localhost:4452/citizens/12345/jurisdictions/PROBATE/case-types/GrantOfRepresentation/" +
                "cases?case.primaryApplicantEmailAddress=test@test.com";
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(ccdRequest),
                eq(JsonNode.class))).thenReturn(response);
        when(requestFactory.createCcdStartRequest(AUTHORIZATION_TOKEN)).thenReturn(ccdRequest);
        ArrayNode arrayNode = objectMapper.createArrayNode();
        arrayNode.add(ccdData);
        when(response.getBody()).thenReturn(arrayNode);
        when(ccdData.get("id")).thenReturn(new LongNode(123));
        when(ccdData.get("state")).thenReturn(new TextNode("STATE"));

        Optional<CcdCaseResponse> optionalCcdCaseResponse = coreCaseDataClient
                .getCase(submitData, USER_ID, AUTHORIZATION_TOKEN);

        assertThat(optionalCcdCaseResponse.isPresent(), is(true));
        verify(restTemplate, times(1)).exchange(eq(url), eq(HttpMethod.GET), eq(ccdRequest),
                eq(JsonNode.class));
        verify(requestFactory, times(1)).createCcdStartRequest(AUTHORIZATION_TOKEN);
    }

    @Test(expected = HttpClientErrorException.class)
    public void shouldThrowHttpClientErrorExceptionOnGetCaseWhenRestTemplateException() {
        String url = "http://localhost:4452/citizens/12345/jurisdictions/PROBATE/case-types/GrantOfRepresentation/" +
                "cases?case.primaryApplicantEmailAddress=test@test.com";
        doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST)).when(restTemplate)
                .exchange(eq(url), eq(HttpMethod.GET), eq(ccdRequest),
                        eq(JsonNode.class));
        when(requestFactory.createCcdStartRequest(AUTHORIZATION_TOKEN)).thenReturn(ccdRequest);
        ArrayNode arrayNode = objectMapper.createArrayNode();
        arrayNode.add(ccdData);
        when(response.getBody()).thenReturn(arrayNode);

        coreCaseDataClient.getCase(submitData, USER_ID, AUTHORIZATION_TOKEN);
    }

    @Test
    public void shouldReturnEmptyOptionalOnGetCaseWhenCaseDoesNotExist() {
        String url = "http://localhost:4452/citizens/12345/jurisdictions/PROBATE/case-types/GrantOfRepresentation/" +
                "cases?case.primaryApplicantEmailAddress=test@test.com";
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(ccdRequest),
                eq(JsonNode.class))).thenReturn(response);
        when(requestFactory.createCcdStartRequest(AUTHORIZATION_TOKEN)).thenReturn(ccdRequest);
        ArrayNode arrayNode = objectMapper.createArrayNode();
        when(response.getBody()).thenReturn(arrayNode);

        Optional<CcdCaseResponse> optionalCcdCaseResponse = coreCaseDataClient
                .getCase(submitData, USER_ID, AUTHORIZATION_TOKEN);

        assertThat(optionalCcdCaseResponse.isPresent(), is(false));
        verify(restTemplate, times(1)).exchange(eq(url), eq(HttpMethod.GET), eq(ccdRequest),
                eq(JsonNode.class));
        verify(requestFactory, times(1)).createCcdStartRequest(AUTHORIZATION_TOKEN);
    }

    @Test
    public void shouldCreatePaymentStatusUpdateEvent() {
        String url = "http://localhost:4452/citizens/12345/jurisdictions/PROBATE/case-types/GrantOfRepresentation/" +
                "cases/9999999/event-triggers/createCase/token";
        when(restTemplate.exchange(url, HttpMethod.GET, ccdRequest, JsonNode.class))
                .thenReturn(response);
        when(requestFactory.createCcdStartRequest(ccdCreateCaseParams.getAuthorization()))
                .thenReturn(ccdRequest);
        when(response.getBody()).thenReturn(ccdData);
        when(ccdData.get(TOKEN_RESOURCE)).thenReturn(tokenJsonNode);

        JsonNode caseTokenJson = coreCaseDataClient
                .createCaseUpdatePaymentStatusEvent(USER_ID, CASE_ID, AUTHORIZATION_TOKEN, UPDATE_PAYMENT_STATUS_CCD_EVENT_ID);

        assertThat(caseTokenJson, is(notNullValue()));
        verify(restTemplate, times(1)).exchange(url, HttpMethod.GET, ccdRequest, JsonNode.class);
        verify(requestFactory, times(1)).createCcdStartRequest(ccdCreateCaseParams.getAuthorization());
    }

    @Test
    public void shouldUpdatePaymentStatus() {
        String url = "http://localhost:4452/citizens/12345/jurisdictions/PROBATE/case-types/GrantOfRepresentation/" +
                "cases/9999999/events";
        when(ccdDataMapper.updatePaymentStatus(paymentResponse,
                UPDATE_PAYMENT_STATUS_CCD_EVENT_ID, tokenJsonNode)).thenReturn(ccdData);
        when(ccdDataMapper
                .updatePaymentStatus(paymentResponse, UPDATE_PAYMENT_STATUS_CCD_EVENT_ID,
                        tokenJsonNode)).thenReturn(ccdData);
        when(requestFactory.createCcdSaveRequest(ccdData, ccdCreateCaseParams.getAuthorization()))
                .thenReturn(ccdRequest);
        when(restTemplate.exchange(url, HttpMethod.POST, ccdRequest, JsonNode.class))
                .thenReturn(response);
        when(response.getBody()).thenReturn(ccdData);
        when(submitData.getCaseId()).thenReturn(CASE_ID);

        CcdCaseResponse updatePaymentStatus = coreCaseDataClient
                .updatePaymentStatus(submitData, USER_ID, AUTHORIZATION_TOKEN, tokenJsonNode,
                        paymentResponse, UPDATE_PAYMENT_STATUS_CCD_EVENT_ID);

        assertThat(updatePaymentStatus, is(notNullValue()));
        verify(ccdDataMapper, times(1)).updatePaymentStatus(paymentResponse,
                UPDATE_PAYMENT_STATUS_CCD_EVENT_ID, tokenJsonNode);
        verify(requestFactory, times(1))
                .createCcdSaveRequest(ccdData, ccdCreateCaseParams.getAuthorization());
        verify(restTemplate, times(1)).exchange(url, HttpMethod.POST, ccdRequest, JsonNode.class);
    }
}
