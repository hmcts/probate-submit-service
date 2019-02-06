package uk.gov.hmcts.probate.services.submit.core;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.probate.security.SecurityDTO;
import uk.gov.hmcts.probate.security.SecurityUtils;
import uk.gov.hmcts.probate.services.submit.model.v2.exception.CaseNotFoundException;
import uk.gov.hmcts.probate.services.submit.model.v2.exception.CaseStatePreconditionException;
import uk.gov.hmcts.probate.services.submit.services.CoreCaseDataService;
import uk.gov.hmcts.reform.probate.model.cases.CaseInfo;
import uk.gov.hmcts.reform.probate.model.cases.CaseState;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantOfRepresentationData;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.probate.model.cases.CaseType.GRANT_OF_REPRESENTATION;

@RunWith(MockitoJUnitRunner.class)
public class SubmissionsServiceImplTest {

    private static final String APPLICANT_EMAIL = "test@test.com";

    private static final String CASE_ID = "12323213323";
    private static final String STATE = "Draft";

    @Mock
    private SecurityUtils mockSecurityUtils;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private SubmissionsServiceImpl submissionsService;

    private ProbateCaseDetails caseRequest;

    private GrantOfRepresentationData caseData;

    private SecurityDTO securityDTO;

    private CaseInfo caseInfo;

    private ProbateCaseDetails caseResponse;

    @Before
    public void setUp() {
        securityDTO = SecurityDTO.builder().build();
        caseData = new GrantOfRepresentationData();
        caseData.setPrimaryApplicantEmailAddress(APPLICANT_EMAIL);
        caseRequest = ProbateCaseDetails.builder().caseData(caseData).build();
        caseInfo = new CaseInfo();
        caseInfo.setCaseId(CASE_ID);
        caseInfo.setState(STATE);
        caseResponse = ProbateCaseDetails.builder().caseData(caseData).caseInfo(caseInfo).build();
    }

    @Test(expected = CaseNotFoundException.class)
    public void shouldThrowCaseNotFoundExceptionWhenNoExistingCase() {
        when(mockSecurityUtils.getSecurityDTO()).thenReturn(securityDTO);
        when(coreCaseDataService.findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDTO))
                .thenReturn(Optional.empty());

        submissionsService.submit(APPLICANT_EMAIL, caseRequest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfEmailsDontMatch() {
        caseData.setPrimaryApplicantEmailAddress("test1234@hello.com");

        submissionsService.submit(APPLICANT_EMAIL, caseRequest);
    }

    @Test
    public void shouldSubmitWhenExistingCase() {
        when(mockSecurityUtils.getSecurityDTO()).thenReturn(securityDTO);
        when(coreCaseDataService.findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDTO))
                .thenReturn(Optional.of(caseResponse));
        when(coreCaseDataService.updateCase(eq(CASE_ID), eq(caseData),
                eq(GRANT_OF_REPRESENTATION.getCaseEvents().getCreateCaseApplicationEventId()), eq(securityDTO)))
                .thenReturn(caseResponse);

        ProbateCaseDetails caseResponse = submissionsService.submit(APPLICANT_EMAIL, caseRequest);

        assertThat(caseResponse.getCaseData(), is(caseData));
        assertThat(caseResponse.getCaseInfo(), is(equalTo(caseInfo)));
        verify(mockSecurityUtils, times(1)).getSecurityDTO();
        verify(coreCaseDataService, times(1)).findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDTO);
        verify(coreCaseDataService, times(1)).updateCase(eq(CASE_ID), eq(caseData),
                eq(GRANT_OF_REPRESENTATION.getCaseEvents().getCreateCaseApplicationEventId()), eq(securityDTO));
    }

    @Test(expected = CaseStatePreconditionException.class)
    public void shouldThrowExceptionWhenStateIsNotDraftWhenSubmitting() {
        caseInfo = new CaseInfo();
        caseInfo.setCaseId(CASE_ID);
        caseInfo.setState(CaseState.PA_APP_CREATED.getName());
        caseResponse = ProbateCaseDetails.builder().caseData(caseData).caseInfo(caseInfo).build();

        when(mockSecurityUtils.getSecurityDTO()).thenReturn(securityDTO);
        when(coreCaseDataService.findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDTO))
                .thenReturn(Optional.of(caseResponse));

        submissionsService.submit(APPLICANT_EMAIL, caseRequest);
    }
}
