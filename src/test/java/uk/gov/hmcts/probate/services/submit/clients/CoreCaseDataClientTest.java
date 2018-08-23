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
    private static final String EVENT_TRIGGERS_RESOURCE = "event-triggers";
    private static final String EVENTS_RESOURCE = "events";
    private static final String TOKEN_RESOURCE = "token";
    private static final String CASES_RESOURCE = "cases";
    private static final String UPDATE_PAYMENT_STATUS_CCD_EVENT_ID = "updatePaymentStatus";
    private static final String CASE_QUERY_PARAM_PREFIX = "case.";
    private static final String PAYMENT_STATUS = "SUCCESSFUL";
    public static final String PRIMARY_APPLICANT_EMAIL_ADDRESS_FIELD = "primaryApplicantEmailAddress";
    public static final String DECEASED_SURNAME_FIELD = "deceasedSurname";
    public static final String DECEASED_FORENAMES_FIELD = "deceasedForenames";
    public static final String DECEASED_DATE_OF_DEATH_FIELD = "deceasedDateOfDeath";
    public static final JsonNode PRIMARY_APPLICANT_EMAIL_ADDRESS = new TextNode("test@test.com");
    public static final JsonNode DECEASED_SURNAME = new TextNode("Brown");
    public static final JsonNode DECEASED_FORENAMES = new TextNode("Bobby");
    public static final JsonNode DECEASED_DATE_OF_DEATH = new TextNode("2000-02-01");

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
    private FormData formData;

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

    @InjectMocks
    private CoreCaseDataClient coreCaseDataClient;

    @Before
    public void setUp() {
        ReflectionTestUtils
                .setField(coreCaseDataClient, "coreCaseDataServiceURL", CORE_CASE_DATA_URL);

        objectMapper = new ObjectMapper();

        ccdCreateCaseParams = new CcdCreateCaseParams.Builder()
                .withAuthorisation(AUTHORIZATION_TOKEN)
                .withFormData(formData)
                .withRegistryData(registryData)
                .withSubmissionReference(SEQUENCE_NUMBER)
                .withSubmitData(submitData)
                .withUserId(USER_ID)
                .withSubmissionTimestamp(SUBMISSION_TIMESTAMP)
                .build();

        when(submitData.getSubmitData()).thenReturn(submitDataJson);
        when(submitDataJson.get(PRIMARY_APPLICANT_EMAIL_ADDRESS_FIELD))
                .thenReturn(PRIMARY_APPLICANT_EMAIL_ADDRESS);
        when(submitDataJson.get(DECEASED_SURNAME_FIELD)).thenReturn(DECEASED_SURNAME);
        when(submitDataJson.get(DECEASED_FORENAMES_FIELD)).thenReturn(DECEASED_FORENAMES);
        when(submitDataJson.get(DECEASED_DATE_OF_DEATH_FIELD)).thenReturn(DECEASED_DATE_OF_DEATH);

        when(ccdCaseResponse.getCaseId()).thenReturn(CASE_ID);
        when(ccdCaseResponse.getCaseData()).thenReturn(ccdData);
    }

    @Test
    public void shouldCreateCase() {
        String url = String.format(CORE_CASE_DATA_URL, USER_ID) + "/" + EVENT_TRIGGERS_RESOURCE + "/" +
                APPLY_FOR_GRANT_CCD_EVENT_ID + "/" + TOKEN_RESOURCE;
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
        String url = String.format(CORE_CASE_DATA_URL, USER_ID) + "/" + EVENT_TRIGGERS_RESOURCE + "/" +
                APPLY_FOR_GRANT_CCD_EVENT_ID + "/" + TOKEN_RESOURCE;
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
        String url = String.format(CORE_CASE_DATA_URL, USER_ID) + "/" + CASES_RESOURCE;
        when(ccdDataMapper.createCcdData(submitDataJson,
                APPLY_FOR_GRANT_CCD_EVENT_ID, tokenJsonNode, ccdCreateCaseParams.getSubmissionTimestamp(),
                ccdCreateCaseParams.getSubmissionReference())).thenReturn(ccdData);
        when(requestFactory.createCcdSaveRequest(ccdData, ccdCreateCaseParams.getAuthorization()))
                .thenReturn(ccdRequest);
        when(restTemplate.exchange(url, HttpMethod.POST, ccdRequest, JsonNode.class))
                .thenReturn(response);

        CcdCaseResponse ccdCaseResponse = coreCaseDataClient
                .saveCase(ccdCreateCaseParams, tokenJsonNode);

        assertThat(ccdCaseResponse, is(notNullValue()));
        verify(ccdDataMapper, times(1)).createCcdData(submitDataJson,
                APPLY_FOR_GRANT_CCD_EVENT_ID, tokenJsonNode, ccdCreateCaseParams.getSubmissionTimestamp(),
                ccdCreateCaseParams.getSubmissionReference());
        verify(requestFactory, times(1))
                .createCcdSaveRequest(ccdData, ccdCreateCaseParams.getAuthorization());
        verify(restTemplate, times(1)).exchange(url, HttpMethod.POST, ccdRequest, JsonNode.class);
    }

    @Test(expected = HttpClientErrorException.class)
    public void shouldThrowHttpClientErrorExceptionOnSaveCaseWhenRestTemplateException() {
        String url = String.format(CORE_CASE_DATA_URL, USER_ID) + "/" + CASES_RESOURCE;
        when(ccdDataMapper.createCcdData(submitDataJson,
                APPLY_FOR_GRANT_CCD_EVENT_ID, tokenJsonNode, ccdCreateCaseParams.getSubmissionTimestamp(),
                ccdCreateCaseParams.getSubmissionReference())).thenReturn(ccdData);
        when(requestFactory.createCcdSaveRequest(ccdData, ccdCreateCaseParams.getAuthorization()))
                .thenReturn(ccdRequest);
        doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST))
                .when(restTemplate).exchange(url, HttpMethod.POST, ccdRequest, JsonNode.class);

        coreCaseDataClient.saveCase(ccdCreateCaseParams, tokenJsonNode);
    }

    @Test
    public void shouldGetCaseWhenExistForQueryParameters() {
        String url = String.format(CORE_CASE_DATA_URL, USER_ID) + "/" + CASES_RESOURCE;
        Map<String, String> queryParameterMap = ImmutableMap.<String, String>builder()
                .put(CASE_QUERY_PARAM_PREFIX + PRIMARY_APPLICANT_EMAIL_ADDRESS_FIELD,
                        PRIMARY_APPLICANT_EMAIL_ADDRESS.textValue())
                .put(CASE_QUERY_PARAM_PREFIX + DECEASED_SURNAME_FIELD, DECEASED_SURNAME.textValue())
                .put(CASE_QUERY_PARAM_PREFIX + DECEASED_FORENAMES_FIELD, DECEASED_FORENAMES.textValue())
                .put(CASE_QUERY_PARAM_PREFIX + DECEASED_DATE_OF_DEATH_FIELD,
                        DECEASED_DATE_OF_DEATH.textValue())
                .build();
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(ccdRequest),
                eq(JsonNode.class), eq(queryParameterMap))).thenReturn(response);
        when(requestFactory.createCcdStartRequest(AUTHORIZATION_TOKEN)).thenReturn(ccdRequest);
        ArrayNode arrayNode = objectMapper.createArrayNode();
        arrayNode.add(ccdData);
        when(response.getBody()).thenReturn(arrayNode);

        Optional<CcdCaseResponse> optionalCcdCaseResponse = coreCaseDataClient
                .getCase(submitData, USER_ID, AUTHORIZATION_TOKEN);

        assertThat(optionalCcdCaseResponse.isPresent(), is(true));
        verify(restTemplate, times(1)).exchange(eq(url), eq(HttpMethod.GET), eq(ccdRequest),
                eq(JsonNode.class), eq(queryParameterMap));
        verify(requestFactory, times(1)).createCcdStartRequest(AUTHORIZATION_TOKEN);
    }

    @Test(expected = HttpClientErrorException.class)
    public void shouldThrowHttpClientErrorExceptionOnGetCaseWhenRestTemplateException() {
        String url = String.format(CORE_CASE_DATA_URL, USER_ID) + "/" + CASES_RESOURCE;
        Map<String, String> queryParameterMap = ImmutableMap.<String, String>builder()
                .put(CASE_QUERY_PARAM_PREFIX + PRIMARY_APPLICANT_EMAIL_ADDRESS_FIELD,
                        PRIMARY_APPLICANT_EMAIL_ADDRESS.textValue())
                .put(CASE_QUERY_PARAM_PREFIX + DECEASED_SURNAME_FIELD, DECEASED_SURNAME.textValue())
                .put(CASE_QUERY_PARAM_PREFIX + DECEASED_FORENAMES_FIELD, DECEASED_FORENAMES.textValue())
                .put(CASE_QUERY_PARAM_PREFIX + DECEASED_DATE_OF_DEATH_FIELD,
                        DECEASED_DATE_OF_DEATH.textValue())
                .build();
        doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST)).when(restTemplate)
                .exchange(eq(url), eq(HttpMethod.GET), eq(ccdRequest),
                        eq(JsonNode.class), eq(queryParameterMap));
        when(requestFactory.createCcdStartRequest(AUTHORIZATION_TOKEN)).thenReturn(ccdRequest);
        ArrayNode arrayNode = objectMapper.createArrayNode();
        arrayNode.add(ccdData);
        when(response.getBody()).thenReturn(arrayNode);

        coreCaseDataClient.getCase(submitData, USER_ID, AUTHORIZATION_TOKEN);
    }

    @Test
    public void shouldReturnEmptyOptionalOnGetCaseWhenCaseDoesNotExist() {
        String url = String.format(CORE_CASE_DATA_URL, USER_ID) + "/" + CASES_RESOURCE;
        Map<String, String> queryParameterMap = ImmutableMap.<String, String>builder()
                .put(CASE_QUERY_PARAM_PREFIX + PRIMARY_APPLICANT_EMAIL_ADDRESS_FIELD,
                        PRIMARY_APPLICANT_EMAIL_ADDRESS.textValue())
                .put(CASE_QUERY_PARAM_PREFIX + DECEASED_SURNAME_FIELD, DECEASED_SURNAME.textValue())
                .put(CASE_QUERY_PARAM_PREFIX + DECEASED_FORENAMES_FIELD, DECEASED_FORENAMES.textValue())
                .put(CASE_QUERY_PARAM_PREFIX + DECEASED_DATE_OF_DEATH_FIELD,
                        DECEASED_DATE_OF_DEATH.textValue())
                .build();
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), eq(ccdRequest),
                eq(JsonNode.class), eq(queryParameterMap))).thenReturn(response);
        when(requestFactory.createCcdStartRequest(AUTHORIZATION_TOKEN)).thenReturn(ccdRequest);
        ArrayNode arrayNode = objectMapper.createArrayNode();
        when(response.getBody()).thenReturn(arrayNode);

        Optional<CcdCaseResponse> optionalCcdCaseResponse = coreCaseDataClient
                .getCase(submitData, USER_ID, AUTHORIZATION_TOKEN);

        assertThat(optionalCcdCaseResponse.isPresent(), is(false));
        verify(restTemplate, times(1)).exchange(eq(url), eq(HttpMethod.GET), eq(ccdRequest),
                eq(JsonNode.class), eq(queryParameterMap));
        verify(requestFactory, times(1)).createCcdStartRequest(AUTHORIZATION_TOKEN);
    }

    @Test
    public void shouldCreatePaymentStatusUpdateEvent() {
        String url = String.format(CORE_CASE_DATA_URL, USER_ID) + "/" + CASES_RESOURCE + "/" + CASE_ID +
                "/" + EVENT_TRIGGERS_RESOURCE + "/" + UPDATE_PAYMENT_STATUS_CCD_EVENT_ID + "/"
                + TOKEN_RESOURCE;
        when(restTemplate.exchange(url, HttpMethod.GET, ccdRequest, JsonNode.class))
                .thenReturn(response);
        when(requestFactory.createCcdStartRequest(ccdCreateCaseParams.getAuthorization()))
                .thenReturn(ccdRequest);
        when(response.getBody()).thenReturn(ccdData);
        when(ccdData.get(TOKEN_RESOURCE)).thenReturn(tokenJsonNode);

        JsonNode caseTokenJson = coreCaseDataClient
                .createCaseUpdatePaymentStatusEvent(USER_ID, CASE_ID, AUTHORIZATION_TOKEN);

        assertThat(caseTokenJson, is(notNullValue()));
        verify(restTemplate, times(1)).exchange(url, HttpMethod.GET, ccdRequest, JsonNode.class);
        verify(requestFactory, times(1)).createCcdStartRequest(ccdCreateCaseParams.getAuthorization());
    }

    @Test
    public void shouldUpdatePaymentStatus() {
        String url =
                String.format(CORE_CASE_DATA_URL, USER_ID) + "/" + CASES_RESOURCE + "/" + CASE_ID + "/" +
                        EVENTS_RESOURCE;
        when(ccdDataMapper.updatePaymentStatus(ccdData, PAYMENT_STATUS,
                UPDATE_PAYMENT_STATUS_CCD_EVENT_ID, tokenJsonNode)).thenReturn(ccdData);
        when(ccdDataMapper
                .updatePaymentStatus(ccdData, PAYMENT_STATUS, UPDATE_PAYMENT_STATUS_CCD_EVENT_ID,
                        tokenJsonNode)).thenReturn(ccdData);
        when(requestFactory.createCcdSaveRequest(ccdData, ccdCreateCaseParams.getAuthorization()))
                .thenReturn(ccdRequest);
        when(restTemplate.exchange(url, HttpMethod.POST, ccdRequest, JsonNode.class))
                .thenReturn(response);
        when(response.getBody()).thenReturn(ccdData);

        JsonNode updatePaymentStatus = coreCaseDataClient
                .updatePaymentStatus(ccdCaseResponse, USER_ID, AUTHORIZATION_TOKEN, tokenJsonNode,
                        PAYMENT_STATUS);

        assertThat(updatePaymentStatus, is(notNullValue()));
        verify(ccdDataMapper, times(1)).updatePaymentStatus(ccdData, PAYMENT_STATUS,
                UPDATE_PAYMENT_STATUS_CCD_EVENT_ID, tokenJsonNode);
        verify(requestFactory, times(1))
                .createCcdSaveRequest(ccdData, ccdCreateCaseParams.getAuthorization());
        verify(restTemplate, times(1)).exchange(url, HttpMethod.POST, ccdRequest, JsonNode.class);
    }
}
