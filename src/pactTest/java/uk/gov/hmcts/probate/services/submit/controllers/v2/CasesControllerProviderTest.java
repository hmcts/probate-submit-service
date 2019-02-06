package uk.gov.hmcts.probate.services.submit.controllers.v2;

import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import org.json.JSONException;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.probate.services.submit.model.v2.exception.CaseNotFoundException;
import uk.gov.hmcts.probate.services.submit.services.CasesService;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;

import java.io.IOException;

import static org.mockito.Mockito.when;

@Provider("probate_submitservice_cases")
public class CasesControllerProviderTest extends ControllerProviderTest {


    @MockBean
    private CasesService casesService;

    @State({"provider returns casedata with success",
            "provider returns casedata with success"})
    public void toReturnCaseDetailsWithSuccess() throws IOException, JSONException {

        when(casesService.getCase("email", CaseType.GRANT_OF_REPRESENTATION))
                .thenReturn(getProbateCaseDetails("intestacyGrantOfRepresentation_full.json"));
    }

    @State({"provider returns casedata not found",
            "provider returns casedata not found"})
    public void toReturnCaseDetailsWithNotFound() {
        when(casesService.getCase("email", CaseType.GRANT_OF_REPRESENTATION))
                .thenThrow(CaseNotFoundException.class);
    }

}