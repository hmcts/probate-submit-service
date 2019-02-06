package uk.gov.hmcts.probate.services.submit.controllers.v2;

import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import org.json.JSONException;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.probate.services.submit.services.SubmissionsService;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Provider("probate_submitservice_submissions")
public class SubmissionsControllerProviderTest extends ControllerProviderTest {

    @MockBean
    private SubmissionsService submissionsService;

    @State({"provider POSTS submission with success",
            "provider POSTS submission with success"})
    public void toPostSubmissionCaseDetailsWithSuccess() throws IOException, JSONException {
        when(submissionsService.submit(anyString(), any(ProbateCaseDetails.class)))
                .thenReturn(getProbateCaseDetails("intestacyGrantOfRepresentation_full.json"));
    }

    @State({"provider POSTS submission with validation errors",
            "provider POSTS submission with validation errors"})
    public void toPostSubmissionCaseDetailsWithValidationErrors() throws IOException, JSONException {
        when(submissionsService.submit(anyString(), any(ProbateCaseDetails.class)))
                .thenReturn(null);
    }
}