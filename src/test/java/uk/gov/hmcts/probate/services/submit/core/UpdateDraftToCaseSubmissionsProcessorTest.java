package uk.gov.hmcts.probate.services.submit.core;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.probate.security.SecurityDTO;
import uk.gov.hmcts.probate.security.SecurityUtils;
import uk.gov.hmcts.probate.services.submit.core.proccessors.impl.UpdateCaseToDraftSubmissionsProcessor;
import uk.gov.hmcts.probate.services.submit.model.v2.exception.CaseNotFoundException;
import uk.gov.hmcts.probate.services.submit.model.v2.exception.CaseStatePreconditionException;
import uk.gov.hmcts.probate.services.submit.services.CoreCaseDataService;
import uk.gov.hmcts.probate.services.submit.validation.CaseDataValidatorFactory;
import uk.gov.hmcts.reform.probate.model.cases.CaseEvents;
import uk.gov.hmcts.reform.probate.model.cases.CaseInfo;
import uk.gov.hmcts.reform.probate.model.cases.CaseState;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.SubmitResult;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantOfRepresentationData;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantType;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.probate.model.cases.CaseType.GRANT_OF_REPRESENTATION;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_CREATE_APPLICATION;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_CREATE_CASE;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_CREATE_DRAFT;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_PAYMENT_FAILED;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_PAYMENT_FAILED_AGAIN;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_PAYMENT_FAILED_TO_SUCCESS;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_UPDATE_DRAFT;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDraftToCaseSubmissionsProcessorTest {

    private static final String APPLICANT_EMAIL = "test@test.com";

    private static final String CASE_ID = "12323213323";
    private static final String STATE = "Draft";

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private EventFactory eventFactory;

    @Mock
    private SearchFieldFactory searchFieldFactory;

    @Mock
    private CaseDataValidatorFactory caseDataValidatorFactory;

    private UpdateCaseToDraftSubmissionsProcessor updateCaseToDraftSubmissionsProcessor;

    private ProbateCaseDetails caseRequest;

    private GrantOfRepresentationData caseData;

    private SecurityDTO securityDTO;

    private CaseInfo caseInfo;

    private ProbateCaseDetails caseResponse;

    @Before
    public void setUp() {
        updateCaseToDraftSubmissionsProcessor = new UpdateCaseToDraftSubmissionsProcessor(
                coreCaseDataService, eventFactory, securityUtils, searchFieldFactory, caseDataValidatorFactory);
        securityDTO = SecurityDTO.builder().build();
        caseData = new GrantOfRepresentationData();
        caseData.setPrimaryApplicantEmailAddress(APPLICANT_EMAIL);
        caseData.setGrantType(GrantType.INTESTACY);
        caseRequest = ProbateCaseDetails.builder().caseData(caseData).build();
        caseInfo = new CaseInfo();
        caseInfo.setCaseId(CASE_ID);
        caseInfo.setState(STATE);
        caseResponse = ProbateCaseDetails.builder().caseData(caseData).caseInfo(caseInfo).build();

        when(searchFieldFactory.getSearchFieldValuePair(CaseType.GRANT_OF_REPRESENTATION, caseData))
                .thenReturn(ImmutablePair.of("primaryApplicantEmailAddress", APPLICANT_EMAIL));

        when(eventFactory.getCaseEvents(CaseType.GRANT_OF_REPRESENTATION)).thenReturn(CaseEvents.builder()
                .createCaseApplicationEventId(GOP_CREATE_APPLICATION)
                .createCaseEventId(GOP_CREATE_CASE)
                .createDraftEventId(GOP_CREATE_DRAFT)
                .paymentFailedAgainEventId(GOP_PAYMENT_FAILED_AGAIN)
                .paymentFailedEventId(GOP_PAYMENT_FAILED)
                .paymentFailedToSuccessEventId(GOP_PAYMENT_FAILED_TO_SUCCESS)
                .updateDraftEventId(GOP_UPDATE_DRAFT)
                .build());
    }

    @Test(expected = CaseNotFoundException.class)
    public void shouldThrowCaseNotFoundExceptionWhenNoExistingCase() {
        when(securityUtils.getSecurityDTO()).thenReturn(securityDTO);
        when(coreCaseDataService.findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDTO))
                .thenReturn(Optional.empty());

        updateCaseToDraftSubmissionsProcessor.process(APPLICANT_EMAIL, caseRequest);
        verify(coreCaseDataService, times(1)).findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDTO);
    }

    @Test
    public void shouldSubmitWhenExistingCase() {
        when(securityUtils.getSecurityDTO()).thenReturn(securityDTO);
        when(coreCaseDataService.findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDTO))
                .thenReturn(Optional.of(caseResponse));
        when(coreCaseDataService.updateCase(eq(CASE_ID), eq(caseData),
                eq(GOP_CREATE_APPLICATION), eq(securityDTO)))
                .thenReturn(caseResponse);

        SubmitResult submitResult = updateCaseToDraftSubmissionsProcessor.process(APPLICANT_EMAIL, caseRequest);
        caseResponse = submitResult.getProbateCaseDetails();
        assertThat(caseResponse.getCaseData(), is(caseData));
        assertThat(caseResponse.getCaseInfo(), is(equalTo(caseInfo)));
        verify(securityUtils, times(1)).getSecurityDTO();
        verify(coreCaseDataService, times(1)).findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDTO);
        verify(coreCaseDataService, times(1)).updateCase(eq(CASE_ID), eq(caseData),
                eq(GOP_CREATE_APPLICATION), eq(securityDTO));
    }

    @Test(expected = CaseStatePreconditionException.class)
    public void shouldThrowExceptionWhenStateIsNotDraftWhenSubmitting() {
        caseInfo = new CaseInfo();
        caseInfo.setCaseId(CASE_ID);
        caseInfo.setState(CaseState.PA_APP_CREATED.getName());
        caseResponse = ProbateCaseDetails.builder().caseData(caseData).caseInfo(caseInfo).build();

        when(securityUtils.getSecurityDTO()).thenReturn(securityDTO);
        when(coreCaseDataService.findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDTO))
                .thenReturn(Optional.of(caseResponse));

        updateCaseToDraftSubmissionsProcessor.process(APPLICANT_EMAIL, caseRequest);

        verify(coreCaseDataService, times(1)).findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDTO);
    }
}
