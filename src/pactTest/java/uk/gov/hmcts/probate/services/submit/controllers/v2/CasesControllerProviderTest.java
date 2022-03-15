package uk.gov.hmcts.probate.services.submit.controllers.v2;

import au.com.dius.pact.provider.junit.target.HttpTarget;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.target.Target;
import au.com.dius.pact.provider.junitsupport.target.TestTarget;
import au.com.dius.pact.provider.spring.SpringRestPactRunner;
import org.json.JSONException;
import org.junit.Before;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.probate.security.SecurityDto;
import uk.gov.hmcts.probate.security.SecurityUtils;
import uk.gov.hmcts.probate.services.submit.services.CoreCaseDataService;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;

import java.io.IOException;
import java.util.Optional;

import static org.mockito.Mockito.when;

@Provider("probate_submitService_cases")
@RunWith(SpringRestPactRunner.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = {
    "server.port=8123", "spring.application.name=PACT_TEST"
})
public class CasesControllerProviderTest extends ControllerProviderTest {

    @TestTarget
    @SuppressWarnings(value = "VisibilityModifier")
    public final Target target = new HttpTarget("http", "localhost", 8123, "/");

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private SecurityUtils securityUtils;

    private SecurityDto securityDto;

    @Before
    public void setUp() {
        securityDto = SecurityDto.builder().build();
        when(securityUtils.getSecurityDto()).thenReturn(securityDto);

    }

    @State({"provider returns casedata with success",
        "provider returns casedata with success"})
    public void toReturnCaseDetailsWithSuccess() throws IOException, JSONException {

        ProbateCaseDetails caseResponse = getProbateCaseDetails("intestacyGrantOfRepresentation_full.json");
        when(coreCaseDataService.findCase("jsnow@bbc.co.uk", CaseType.GRANT_OF_REPRESENTATION, securityDto))
            .thenReturn(Optional.of(caseResponse));
    }

    @State({"an invite has been sent for a case",
        "provider returns casedata with success"})
    public void toReturnCaseDataByInviteIdWithSuccess() throws IOException, JSONException {

        when(coreCaseDataService.findCaseByInviteId("654321", CaseType.GRANT_OF_REPRESENTATION, securityDto))
            .thenReturn(Optional.of(
                getProbateCaseDetails("probate_orchestrator_service_invite_search_response.json")));


    }

    @State({"provider returns casedata not found",
        "provider returns casedata not found"})
    public void toReturnCaseDetailsWithNotFound() {

        when(coreCaseDataService.findCase("jsnow@bbc.co.uk", CaseType.GRANT_OF_REPRESENTATION, securityDto))
            .thenReturn(Optional.empty());
    }

}