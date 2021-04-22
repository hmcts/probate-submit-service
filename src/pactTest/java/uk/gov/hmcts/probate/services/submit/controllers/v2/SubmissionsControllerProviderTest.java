package uk.gov.hmcts.probate.services.submit.controllers.v2;

import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.target.HttpTarget;
import au.com.dius.pact.provider.junit.target.Target;
import au.com.dius.pact.provider.junit.target.TestTarget;
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
import uk.gov.hmcts.reform.probate.model.cases.CaseInfo;
import uk.gov.hmcts.reform.probate.model.cases.EventId;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantOfRepresentationData;
import uk.gov.hmcts.reform.probate.model.client.ApiClientError;
import uk.gov.hmcts.reform.probate.model.client.ApiClientErrorResponse;
import uk.gov.hmcts.reform.probate.model.client.ApiClientException;
import uk.gov.hmcts.reform.probate.model.client.ErrorResponse;

import java.io.IOException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.probate.model.cases.CaseType.GRANT_OF_REPRESENTATION;

@Provider("probate_submitservice_submissions")
@RunWith(SpringRestPactRunner.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = {
    "server.port=8125", "spring.application.name=PACT_TEST"
})
public class SubmissionsControllerProviderTest extends ControllerProviderTest {

    private static final String APPLICANT_EMAIL = "jsnow@bbc.co.uk";

    private static final String CASE_ID = "12323213323";
    private static final String STATE = "Draft";
    @TestTarget
    @SuppressWarnings(value = "VisibilityModifier")
    public final Target target = new HttpTarget("http", "localhost", 8125, "/");

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private SecurityUtils securityUtils;

    private ProbateCaseDetails caseRequest;

    private GrantOfRepresentationData caseData;

    private SecurityDto securityDto;

    private CaseInfo caseInfo;

    private ProbateCaseDetails caseResponse;

    @Before
    public void setUp() {
        securityDto = SecurityDto.builder().build();
        when(securityUtils.getSecurityDto()).thenReturn(securityDto);

    }

    @State({"provider POSTS submission with success",
        "provider POSTS  submission with success"})
    public void toPostSubmissionCaseDetailsWithSuccess() throws IOException, JSONException {

        caseResponse = getProbateCaseDetails("intestacyGrantOfRepresentation_full_submission.json");

        when(coreCaseDataService.findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDto))
            .thenReturn(Optional.of(caseResponse));
        when(coreCaseDataService.updateCase(anyString(), any(GrantOfRepresentationData.class),
            any(EventId.class), any(SecurityDto.class)))
            .thenReturn(caseResponse);

    }

    @State({"provider POSTS submission with errors",
        "provider POSTS submission with errors"})
    public void verifyExecutePostSubmissionWithClientErrors() {

        ApiClientError apiClientError = new ApiClientError();
        apiClientError.setException("uk.gov.hmcts.ccd.endpoint.exceptions.ResourceNotFoundException");
        apiClientError.setStatus(400);
        apiClientError.setError("Not Found");
        apiClientError.setPath("/citizens/36/jurisdictions/PROBATE/case-types/GrantOfRepresentation/cases");

        ErrorResponse errorResponse = new ApiClientErrorResponse(apiClientError);
        ApiClientException apiClientException = new ApiClientException(400, errorResponse);

        when(coreCaseDataService.findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDto))
            .thenThrow(apiClientException);
    }
}