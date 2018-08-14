package uk.gov.hmcts.probate.services.submit.services;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import uk.gov.hmcts.probate.services.submit.clients.MailClient;
import uk.gov.hmcts.probate.services.submit.clients.PersistenceClient;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import uk.gov.hmcts.probate.services.submit.clients.CoreCaseDataClient;

public class SubmitServiceTest {

    private SubmitService submitService;
    private MailClient mockMailClient;
    private PersistenceClient persistenceClient;
    private CoreCaseDataClient coreCaseDataClient;
    private SequenceService sequenceService;
    private Calendar submissionTimestamp;
    private JsonNode registryData;

    @Before
    public void setUp() throws Exception {
        persistenceClient = mock(PersistenceClient.class);
        mockMailClient = mock(MailClient.class);
        coreCaseDataClient = mock(CoreCaseDataClient.class);
        sequenceService = mock(SequenceService.class);
        submitService = new SubmitService(mockMailClient, persistenceClient, coreCaseDataClient, sequenceService);
        submissionTimestamp = Calendar.getInstance();
        registryData = TestUtils.getJsonNodeFromFile("registryDataSubmit.json");
    }

    @Test
    public void testSubmitWithSuccess() throws IOException {
        String userId = "123";
        String authorizationToken = "dummyAuthToken";
        JsonNode submitData = TestUtils.getJsonNodeFromFile("formPayload.json");
        when(persistenceClient.loadFormDataById(anyString())).thenReturn(submitData);
        when(persistenceClient.saveSubmission(submitData)).thenReturn(submitData);
        when(mockMailClient.execute(submitData, registryData, submissionTimestamp)).thenReturn("12345678");
        when(sequenceService.nextRegistry(submitData.get("id").asLong())).thenReturn(registryData);
        JsonNode dummmyCcdStartCaseRespose =  TestUtils.getJsonNodeFromFile("ccdStartCaseResponse.json");

        JsonNode response = submitService.submit(submitData, userId, authorizationToken);

        assertThat(response, is(registryData));
    }

    @Test
    public void testResubmitWithSuccess() throws IOException {
        JsonNode resubmitData = TestUtils.getJsonNodeFromFile("formPayload.json");
        JsonNode formData = TestUtils.getJsonNodeFromFile("formData.json");
        JsonNode registryData = TestUtils.getJsonNodeFromFile("registryDataResubmitNewApplication.json");
        when(persistenceClient.loadSubmission(Long.parseLong("112233"))).thenReturn(resubmitData);
        when(persistenceClient.loadFormDataBySubmissionReference(Long.parseLong("112233"))).thenReturn(formData);
        when(sequenceService.populateRegistryResubmitData(Long.parseLong("112233"), formData)).thenReturn(registryData);
        when(mockMailClient.execute(eq(resubmitData), eq(registryData), any(Calendar.class) )).thenReturn("12345678");

        String response = submitService.resubmit(Long.parseLong("112233"));

        assertThat(response, is("12345678"));
    }

    @Test
    public void testResubmitWithFailure() {
        doThrow(HttpClientErrorException.class).when(persistenceClient).loadSubmission(999);
        String response = submitService.resubmit(Long.parseLong("999"));
        assertThat(response, is("Invalid submission reference entered.  Please enter a valid submission reference."));
    }
}