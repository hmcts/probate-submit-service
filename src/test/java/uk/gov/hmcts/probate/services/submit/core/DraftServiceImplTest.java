package uk.gov.hmcts.probate.services.submit.core;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.probate.security.SecurityDTO;
import uk.gov.hmcts.probate.security.SecurityUtils;
import uk.gov.hmcts.probate.services.submit.services.CoreCaseDataService;
import uk.gov.hmcts.reform.probate.model.cases.CaseEvents;
import uk.gov.hmcts.reform.probate.model.cases.CaseInfo;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.EventId;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantOfRepresentationData;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
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
public class DraftServiceImplTest {

    private static final String APPLICANT_EMAIL = "test@test.com";

    private static final String CASE_ID = "12323213323";

    private static final String STATE = "STATE";

    private static final EventId CREATE_DRAFT = EventId.GOP_CREATE_DRAFT;

    private static final EventId UPDATE_DRAFT = EventId.GOP_UPDATE_DRAFT;

    @Mock
    private SecurityUtils mockSecurityUtils;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private EventFactory eventFactory;

    @Mock
    private SearchFieldFactory searchFieldFactory;

    @InjectMocks
    private DraftServiceImpl draftService;

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

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfEmailsDontMatch() {
        caseData.setPrimaryApplicantEmailAddress("test1234@hello.com");
        when(searchFieldFactory.getSearchFieldValuePair(CaseType.GRANT_OF_REPRESENTATION, caseData))
                .thenReturn(ImmutablePair.of("primaryApplicantEmailAddress", "test1234@hello.com"));

        draftService.saveDraft(APPLICANT_EMAIL, caseRequest);
    }

    @Test
    public void shouldCreateDraftWhenNoExistingCase() {
        when(mockSecurityUtils.getSecurityDTO()).thenReturn(securityDTO);
        when(coreCaseDataService.findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDTO))
                .thenReturn(Optional.empty());
        when(coreCaseDataService.createCase(caseData, CREATE_DRAFT, securityDTO)).thenReturn(caseResponse);

        ProbateCaseDetails caseResponse = draftService.saveDraft(APPLICANT_EMAIL, caseRequest);

        assertThat(caseResponse.getCaseData(), is(caseData));
        assertThat(caseResponse.getCaseInfo(), is(caseInfo));
        verify(mockSecurityUtils, times(1)).getSecurityDTO();
        verify(coreCaseDataService, times(1)).findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDTO);
        verify(coreCaseDataService, times(1)).createCase(caseData, CREATE_DRAFT, securityDTO);
    }

    @Test
    public void shouldUpdateDraftWhenExistingCase() {
        when(mockSecurityUtils.getSecurityDTO()).thenReturn(securityDTO);
        when(coreCaseDataService.findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDTO))
                .thenReturn(Optional.of(caseResponse));
        when(coreCaseDataService.updateCase(CASE_ID, caseData, UPDATE_DRAFT, securityDTO)).thenReturn(caseResponse);

        ProbateCaseDetails caseResponse = draftService.saveDraft(APPLICANT_EMAIL, caseRequest);

        assertThat(caseResponse.getCaseData(), is(caseData));
        assertThat(caseResponse.getCaseInfo(), is(equalTo(caseInfo)));
        verify(mockSecurityUtils, times(1)).getSecurityDTO();
        verify(coreCaseDataService, times(1)).findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDTO);
        verify(coreCaseDataService, times(1)).updateCase(CASE_ID, caseData, UPDATE_DRAFT, securityDTO);
    }
}
