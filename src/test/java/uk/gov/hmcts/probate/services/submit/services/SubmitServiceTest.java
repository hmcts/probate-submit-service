package uk.gov.hmcts.probate.services.submit.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.probate.services.submit.clients.CoreCaseDataClient;
import uk.gov.hmcts.probate.services.submit.clients.MailClient;
import uk.gov.hmcts.probate.services.submit.clients.PersistenceClient;
import uk.gov.hmcts.probate.services.submit.model.CcdCaseResponse;
import uk.gov.hmcts.probate.services.submit.model.FormData;
import uk.gov.hmcts.probate.services.submit.model.PaymentResponse;
import uk.gov.hmcts.probate.services.submit.model.PersistenceResponse;
import uk.gov.hmcts.probate.services.submit.model.SubmitData;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;

import java.util.Calendar;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SubmitServiceTest {

    private static final String USER_ID = "12345";
    private static final Long CASE_ID = 99999L;
    private static final String CASE_STATE = "CCD_STATE";
    private static final Long ID = 1L;
    private static final String AUTHORIZATION_TOKEN = "XXXXXX";
    private static final String APPLICANT_EMAIL_ADDRESS = "test@test.com";

    private SubmitService submitService;

    @Mock
    private MailClient mockMailClient;

    @Mock
    private PersistenceClient persistenceClient;

    @Mock
    private CoreCaseDataClient coreCaseDataClient;

    @Mock
    private SequenceService sequenceService;

    @Mock
    private SubmitData submitData;

    @Mock
    private FormData formData;

    @Mock
    private JsonNode formDataJson;

    @Mock
    private JsonNode submissionReference;

    @Mock
    private JsonNode jsonNode;

    @Mock
    private PersistenceResponse persistenceResponse;

    @Mock
    private CcdCaseResponse ccdCaseResponse;

    @Mock
    private PaymentResponse paymentResponse;

    private ObjectMapper objectMapper;

    private Calendar submissionTimestamp;
    private JsonNode registryData;

    @Before
    public void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        submissionTimestamp = Calendar.getInstance();
        registryData = TestUtils.getJsonNodeFromFile("registryDataSubmit.json");
        submitService = new SubmitService(mockMailClient, persistenceClient, coreCaseDataClient,
                sequenceService, objectMapper);
        ReflectionTestUtils.setField(submitService, "coreCaseDataEnabled", true);
    }

    @Test
    public void shouldReturnDuplicateSubmissionWhenSubmissionReferenceExists() {
        when(submitData.getApplicantEmailAddress()).thenReturn(APPLICANT_EMAIL_ADDRESS);
        when(formData.getSubmissionReference()).thenReturn(12345L);
        when(persistenceClient.loadFormDataById(APPLICANT_EMAIL_ADDRESS)).thenReturn(formData);

        JsonNode submitResponse = submitService.submit(submitData, USER_ID, AUTHORIZATION_TOKEN);

        assertThat(submitResponse.asText(), is("DUPLICATE_SUBMISSION"));
    }

    @Test
    public void shouldSubmitSuccessfullyWhenCaseAlreadyExists() {
        when(submitData.getApplicantEmailAddress()).thenReturn(APPLICANT_EMAIL_ADDRESS);
        when(formData.getSubmissionReference()).thenReturn(0L);
        when(persistenceClient.loadFormDataById(APPLICANT_EMAIL_ADDRESS)).thenReturn(formData);
        when(persistenceClient.saveSubmission(submitData)).thenReturn(persistenceResponse);
        when(persistenceResponse.getIdAsLong()).thenReturn(ID);
        when(persistenceResponse.getIdAsJsonNode()).thenReturn(submissionReference);
        when(submissionReference.asLong()).thenReturn(ID);
        when(sequenceService.nextRegistry(ID)).thenReturn(registryData);
        when(ccdCaseResponse.getCaseId()).thenReturn(CASE_ID);
        when(ccdCaseResponse.getState()).thenReturn(CASE_STATE);
        when(formData.getJson()).thenReturn(formDataJson);
        Optional<CcdCaseResponse> caseResponseOptional = Optional.of(ccdCaseResponse);
        when(coreCaseDataClient.getCase(submitData, USER_ID, AUTHORIZATION_TOKEN)).thenReturn(caseResponseOptional);

        JsonNode submitResponse = submitService.submit(submitData, USER_ID, AUTHORIZATION_TOKEN);

        assertThat(submitResponse, is(notNullValue()));
        assertThat(submitResponse.at("/caseId").longValue(), is(equalTo(CASE_ID)));
        assertThat(submitResponse.at("/caseState").asText(), is(equalTo(CASE_STATE)));
        assertThat(submitResponse.at("/registry"), is(equalTo(registryData)));
        verify(persistenceClient, times(1)).updateFormData(APPLICANT_EMAIL_ADDRESS, ID, formDataJson);
        verify(persistenceClient, times(1)).loadFormDataById(APPLICANT_EMAIL_ADDRESS);
        verify(persistenceClient, times(1)).saveSubmission(submitData);
        verify(coreCaseDataClient, times(1)).getCase(submitData, USER_ID, AUTHORIZATION_TOKEN);
        verify(coreCaseDataClient, never()).createCase(any());
        verify(coreCaseDataClient, never()).saveCase(any(), any());
        verify(sequenceService, times(1)).nextRegistry(ID);
    }

    @Test
    public void shouldSubmitSuccessfullyAfterCreatingCase() {
        when(submitData.getApplicantEmailAddress()).thenReturn(APPLICANT_EMAIL_ADDRESS);
        when(formData.getSubmissionReference()).thenReturn(0L);
        when(persistenceClient.loadFormDataById(APPLICANT_EMAIL_ADDRESS)).thenReturn(formData);
        when(persistenceClient.saveSubmission(submitData)).thenReturn(persistenceResponse);
        when(persistenceResponse.getIdAsLong()).thenReturn(ID);
        when(submitData.getPaymentTotal()).thenReturn(100D);
        when(persistenceResponse.getIdAsJsonNode()).thenReturn(submissionReference);
        when(submissionReference.asLong()).thenReturn(ID);
        when(sequenceService.nextRegistry(ID)).thenReturn(registryData);
        when(ccdCaseResponse.getCaseId()).thenReturn(CASE_ID);
        when(ccdCaseResponse.getState()).thenReturn(CASE_STATE);
        when(formData.getJson()).thenReturn(formDataJson);
        Optional<CcdCaseResponse> caseResponseOptional = Optional.empty();
        when(coreCaseDataClient.getCase(submitData, USER_ID, AUTHORIZATION_TOKEN)).thenReturn(caseResponseOptional);
        when(coreCaseDataClient.saveCase(any(), any())).thenReturn(ccdCaseResponse);
        when(coreCaseDataClient.createCase(any())).thenReturn(jsonNode);

        JsonNode submitResponse = submitService.submit(submitData, USER_ID, AUTHORIZATION_TOKEN);

        assertThat(submitResponse, is(notNullValue()));
        assertThat(submitResponse.at("/caseId").longValue(), is(equalTo(CASE_ID)));
        assertThat(submitResponse.at("/caseState").asText(), is(equalTo(CASE_STATE)));
        assertThat(submitResponse.at("/registry"), is(equalTo(registryData)));
        verify(persistenceClient, times(1)).updateFormData(APPLICANT_EMAIL_ADDRESS, ID, formDataJson);
        verify(persistenceClient, times(1)).loadFormDataById(APPLICANT_EMAIL_ADDRESS);
        verify(persistenceClient, times(1)).saveSubmission(submitData);
        verify(coreCaseDataClient, times(1)).getCase(submitData, USER_ID, AUTHORIZATION_TOKEN);
        verify(coreCaseDataClient, times(1)).createCase(any());
        verify(coreCaseDataClient, times(1)).saveCase(any(), any());
        verify(mockMailClient, never()).execute(any(), any(), any());
        verify(sequenceService, times(1)).nextRegistry(ID);
    }

    @Test
    public void shouldNotSubmitToCcdIfCcdIsDisabled() {
        ReflectionTestUtils.setField(submitService, "coreCaseDataEnabled", false);

        when(submitData.getApplicantEmailAddress()).thenReturn(APPLICANT_EMAIL_ADDRESS);
        when(submitData.getPaymentTotal()).thenReturn(100D);
        when(formData.getSubmissionReference()).thenReturn(0L);
        when(persistenceClient.loadFormDataById(APPLICANT_EMAIL_ADDRESS)).thenReturn(formData);
        when(persistenceClient.saveSubmission(submitData)).thenReturn(persistenceResponse);
        when(persistenceResponse.getIdAsLong()).thenReturn(ID);
        when(persistenceResponse.getIdAsJsonNode()).thenReturn(submissionReference);
        when(submissionReference.asLong()).thenReturn(ID);
        when(sequenceService.nextRegistry(ID)).thenReturn(registryData);
        when(ccdCaseResponse.getCaseId()).thenReturn(CASE_ID);
        when(formData.getJson()).thenReturn(formDataJson);
        Optional<CcdCaseResponse> caseResponseOptional = Optional.empty();
        when(coreCaseDataClient.getCase(submitData, USER_ID, AUTHORIZATION_TOKEN)).thenReturn(caseResponseOptional);

        JsonNode submitResponse = submitService.submit(submitData, USER_ID, AUTHORIZATION_TOKEN);

        assertThat(submitResponse, is(notNullValue()));
        verify(persistenceClient, times(1)).updateFormData(APPLICANT_EMAIL_ADDRESS, ID, formDataJson);
        verify(persistenceClient, times(1)).loadFormDataById(APPLICANT_EMAIL_ADDRESS);
        verify(persistenceClient, times(1)).saveSubmission(submitData);
        verify(coreCaseDataClient, never()).getCase(submitData, USER_ID, AUTHORIZATION_TOKEN);
        verify(coreCaseDataClient, never()).createCase(any());
        verify(coreCaseDataClient, never()).saveCase(any(), any());
        verify(mockMailClient, never()).execute(any(), any(), any());
        verify(sequenceService, times(1)).nextRegistry(ID);
    }

    @Test
    public void shouldSubmitSuccessfullyAfterCreatingCaseAndSendEmail() {
        when(submitData.getApplicantEmailAddress()).thenReturn(APPLICANT_EMAIL_ADDRESS);
        when(formData.getSubmissionReference()).thenReturn(0L);
        when(persistenceClient.loadFormDataById(APPLICANT_EMAIL_ADDRESS)).thenReturn(formData);
        when(persistenceClient.saveSubmission(submitData)).thenReturn(persistenceResponse);
        when(persistenceResponse.getIdAsLong()).thenReturn(ID);
        when(submitData.getPaymentTotal()).thenReturn(0D);
        when(persistenceResponse.getIdAsJsonNode()).thenReturn(submissionReference);
        when(submissionReference.asLong()).thenReturn(ID);
        when(sequenceService.nextRegistry(ID)).thenReturn(registryData);
        when(ccdCaseResponse.getCaseId()).thenReturn(CASE_ID);
        when(formData.getJson()).thenReturn(formDataJson);
        Optional<CcdCaseResponse> caseResponseOptional = Optional.empty();
        when(coreCaseDataClient.getCase(submitData, USER_ID, AUTHORIZATION_TOKEN)).thenReturn(caseResponseOptional);
        when(coreCaseDataClient.saveCase(any(), any())).thenReturn(ccdCaseResponse);
        when(coreCaseDataClient.createCase(any())).thenReturn(jsonNode);

        JsonNode submitResponse = submitService.submit(submitData, USER_ID, AUTHORIZATION_TOKEN);

        assertThat(submitResponse, is(notNullValue()));
        verify(persistenceClient, times(1)).updateFormData(APPLICANT_EMAIL_ADDRESS, ID, formDataJson);
        verify(persistenceClient, times(1)).loadFormDataById(APPLICANT_EMAIL_ADDRESS);
        verify(persistenceClient, times(1)).saveSubmission(submitData);
        verify(coreCaseDataClient, times(1)).getCase(submitData, USER_ID, AUTHORIZATION_TOKEN);
        verify(coreCaseDataClient, times(1)).createCase(any());
        verify(coreCaseDataClient, times(1)).saveCase(any(), any());
        verify(mockMailClient, never()).execute(any(), any(), any());
        verify(sequenceService, times(1)).nextRegistry(ID);
    }


    @Test
    public void shouldUpdatePaymentStatusSuccessfully() {
        when(submitData.getApplicantEmailAddress()).thenReturn(APPLICANT_EMAIL_ADDRESS);
        when(submitData.getPaymentResponse()).thenReturn(paymentResponse);
        when(submissionReference.asLong()).thenReturn(ID);
        when(formData.getSubmissionReference()).thenReturn(0L);
        when(submitData.getCaseId()).thenReturn(CASE_ID);
        when(coreCaseDataClient.createCaseUpdatePaymentStatusEvent(USER_ID, CASE_ID, AUTHORIZATION_TOKEN)).thenReturn(jsonNode);
        Optional<CcdCaseResponse> caseResponseOptional = Optional.of(ccdCaseResponse);
        when(coreCaseDataClient.getCase(submitData, USER_ID, AUTHORIZATION_TOKEN)).thenReturn(caseResponseOptional);
        when(coreCaseDataClient.updatePaymentStatus(CASE_ID, USER_ID, AUTHORIZATION_TOKEN, jsonNode, paymentResponse)).thenReturn(ccdCaseResponse);
        when(sequenceService.nextRegistry(ID)).thenReturn(registryData);
        when(persistenceClient.loadFormDataById(APPLICANT_EMAIL_ADDRESS)).thenReturn(formData);

        JsonNode submitResponse = submitService.updatePaymentStatus(submitData, USER_ID, AUTHORIZATION_TOKEN);

        assertThat(submitResponse, is(notNullValue()));
        verify(coreCaseDataClient, times(1)).createCaseUpdatePaymentStatusEvent(USER_ID, CASE_ID, AUTHORIZATION_TOKEN);
        verify(coreCaseDataClient, times(1)).updatePaymentStatus(CASE_ID, USER_ID, AUTHORIZATION_TOKEN, jsonNode, paymentResponse);
        verify(mockMailClient, times(1)).execute(any(), any(), any());
    }

    @Test
    public void shouldResubmitWithSuccess() {
        when(persistenceClient.loadSubmission(Long.parseLong("112233"))).thenReturn(jsonNode);
        when(persistenceClient.loadFormDataBySubmissionReference(Long.parseLong("112233"))).thenReturn(formDataJson);
        when(sequenceService.populateRegistryResubmitData(Long.parseLong("112233"), formDataJson)).thenReturn(registryData);
        when(mockMailClient.execute(eq(jsonNode), eq(registryData), any(Calendar.class))).thenReturn("12345678");

        String response = submitService.resubmit(Long.parseLong("112233"));

        assertThat(response, is("12345678"));
    }

    @Test
    public void shouldResubmitWithFailureWhenPersistenceClientThrowsException() {
        doThrow(HttpClientErrorException.class).when(persistenceClient).loadSubmission(999);
        String response = submitService.resubmit(Long.parseLong("999"));

        assertThat(response, is("Invalid submission reference entered.  Please enter a valid submission reference."));
    }
}