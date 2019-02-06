package uk.gov.hmcts.probate.services.submit.controllers.v2;

import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.probate.services.submit.services.DraftService;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Provider("probate_submitservice_drafts")
public class DraftsControllerProviderTest extends ControllerProviderTest {

    @MockBean
    private DraftService draftService;

    @State({"provider POSTS draft casedata with success",
            "provider POSTS draft casedata with success"})
    public void toPostDraftCaseDetailsWithSuccess() throws IOException, JSONException {

        when(draftService.saveDraft(anyString(),any(ProbateCaseDetails.class)))
                .thenReturn(getProbateCaseDetails("intestacyGrantOfRepresentation_full.json"));
    }

    @State({"provider POSTS partial draft casedata with success",
            "provider POSTS partial draft casedata with success"})
    public void toPostPartialDraftCaseDetailsWithSuccess() throws IOException, JSONException {

        when(draftService.saveDraft(anyString(),any(ProbateCaseDetails.class)))
                .thenReturn(getProbateCaseDetails("intestacyGrantOfRepresentation_partial_draft.json"));
    }

}