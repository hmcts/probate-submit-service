package uk.gov.hmcts.probate.services.submit.core;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.probate.security.SecurityDto;
import uk.gov.hmcts.probate.security.SecurityUtils;
import uk.gov.hmcts.probate.services.submit.services.CoreCaseDataService;
import uk.gov.hmcts.probate.services.submit.services.ValidationService;
import uk.gov.hmcts.reform.probate.model.cases.CaseEvents;
import uk.gov.hmcts.reform.probate.model.cases.CaseInfo;
import uk.gov.hmcts.reform.probate.model.cases.CaseState;
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
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_CREATE_CASE_WITHOUT_PAYMENT;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_CREATE_DRAFT;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_PAYMENT_FAILED;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_PAYMENT_FAILED_AGAIN;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_PAYMENT_FAILED_TO_SUCCESS;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_UPDATE_APPLICATION;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_UPDATE_DRAFT;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.UPDATE_GOP_PAYMENT_FAILED;

@RunWith(MockitoJUnitRunner.class)
public class CasesServiceImplTest {

    private static final String EMAIL_ADDRESS = "test@test.com";
    private static final String INVITATION_ID = "inviationId";
    private static final String CASE_ID = "1343242352";
    private static final CaseType CASE_TYPE = CaseType.GRANT_OF_REPRESENTATION;

    private static final EventId CREATE_DRAFT = EventId.GOP_CREATE_DRAFT;

    private static final EventId UPDATE_DRAFT = EventId.GOP_UPDATE_DRAFT;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private ValidationService validationService;

    @InjectMocks
    private CasesServiceImpl casesService;

    @Mock
    private SearchFieldFactory searchFieldFactory;

    @Mock
    private EventFactory eventFactory;

    @Before
    public void setUp() {
        when(eventFactory.getCaseEvents(GRANT_OF_REPRESENTATION)).thenReturn(CaseEvents.builder()
            .createCaseApplicationEventId(GOP_CREATE_APPLICATION)
            .updateCaseApplicationEventId(GOP_UPDATE_APPLICATION)
            .createCaseEventId(GOP_CREATE_CASE)
            .createDraftEventId(GOP_CREATE_DRAFT)
            .paymentFailedAgainEventId(GOP_PAYMENT_FAILED_AGAIN)
            .paymentFailedEventId(GOP_PAYMENT_FAILED)
            .paymentFailedToSuccessEventId(GOP_PAYMENT_FAILED_TO_SUCCESS)
            .updateDraftEventId(GOP_UPDATE_DRAFT)
            .createCaseWithoutPaymentId(GOP_CREATE_CASE_WITHOUT_PAYMENT)
            .updatePaymentFailedEventId(UPDATE_GOP_PAYMENT_FAILED)
            .build());
    }

    @Test
    public void shouldGetCase() {
        SecurityDto securityDto = SecurityDto.builder().build();
        Optional<ProbateCaseDetails> caseResponseOptional = Optional.of(ProbateCaseDetails.builder().build());
        when(securityUtils.getSecurityDto()).thenReturn(securityDto);
        when(coreCaseDataService.findCase(EMAIL_ADDRESS, CASE_TYPE, securityDto)).thenReturn(caseResponseOptional);

        ProbateCaseDetails caseResponse = casesService.getCase(EMAIL_ADDRESS, CASE_TYPE);

        assertThat(caseResponse, equalTo(caseResponseOptional.get()));
        verify(securityUtils, times(1)).getSecurityDto();
        verify(coreCaseDataService, times(1)).findCase(EMAIL_ADDRESS, CASE_TYPE, securityDto);
    }

    @Test
    public void shouldGetCaseByApplicantEmail() {
        SecurityDto securityDto = SecurityDto.builder().build();
        Optional<ProbateCaseDetails> caseResponseOptional = Optional.of(ProbateCaseDetails.builder().build());
        when(securityUtils.getSecurityDto()).thenReturn(securityDto);
        when(coreCaseDataService.findCaseByApplicantEmail(EMAIL_ADDRESS, CASE_TYPE, securityDto))
            .thenReturn(caseResponseOptional);

        ProbateCaseDetails caseResponse = casesService.getCaseByApplicantEmail(EMAIL_ADDRESS, CASE_TYPE);

        assertThat(caseResponse, equalTo(caseResponseOptional.get()));
        verify(securityUtils, times(1)).getSecurityDto();
        verify(coreCaseDataService, times(1)).findCaseByApplicantEmail(EMAIL_ADDRESS, CASE_TYPE, securityDto);
    }

    @Test
    public void shouldGetCaseByInvitationId() {
        SecurityDto securityDto = SecurityDto.builder().build();
        Optional<ProbateCaseDetails> caseResponseOptional = Optional.of(ProbateCaseDetails.builder().build());
        when(securityUtils.getSecurityDto()).thenReturn(securityDto);
        when(coreCaseDataService.findCaseByInviteId(INVITATION_ID, CASE_TYPE, securityDto))
            .thenReturn(caseResponseOptional);

        ProbateCaseDetails caseResponse = casesService.getCaseByInvitationId(INVITATION_ID, CASE_TYPE);

        assertThat(caseResponse, equalTo(caseResponseOptional.get()));
        verify(securityUtils, times(1)).getSecurityDto();
        verify(coreCaseDataService, times(1)).findCaseByInviteId(INVITATION_ID, CASE_TYPE, securityDto);
    }

    @Test
    public void shouldValidate() {
        SecurityDto securityDto = SecurityDto.builder().build();
        Optional<ProbateCaseDetails> caseResponseOptional = Optional.of(ProbateCaseDetails.builder().build());
        when(securityUtils.getSecurityDto()).thenReturn(securityDto);
        when(coreCaseDataService.findCase(EMAIL_ADDRESS, CASE_TYPE, securityDto)).thenReturn(caseResponseOptional);

        casesService.validate(EMAIL_ADDRESS, CASE_TYPE);

        verify(validationService, times(1)).validate(caseResponseOptional.get());
    }

    @Test
    public void shouldGetCaseById() {
        SecurityDto securityDto = SecurityDto.builder().build();
        Optional<ProbateCaseDetails> caseResponseOptional = Optional.of(ProbateCaseDetails.builder().build());
        when(securityUtils.getSecurityDto()).thenReturn(securityDto);
        when(coreCaseDataService.findCaseById(CASE_ID, securityDto)).thenReturn(caseResponseOptional);

        ProbateCaseDetails caseResponse = casesService.getCaseById(CASE_ID);

        assertThat(caseResponse, equalTo(caseResponseOptional.get()));
        verify(securityUtils, times(1)).getSecurityDto();
        verify(coreCaseDataService, times(1)).findCaseById(CASE_ID, securityDto);
    }

    @Test
    public void shouldGetAllCases() {
        SecurityDto securityDto = SecurityDto.builder().build();
        Optional<ProbateCaseDetails> caseResponseOptional = Optional.of(ProbateCaseDetails.builder().build());
        when(securityUtils.getSecurityDto()).thenReturn(securityDto);
        when(coreCaseDataService.findCaseById(CASE_ID, securityDto)).thenReturn(caseResponseOptional);

        ProbateCaseDetails caseResponse = casesService.getCaseById(CASE_ID);

        assertThat(caseResponse, equalTo(caseResponseOptional.get()));
        verify(securityUtils, times(1)).getSecurityDto();
        verify(coreCaseDataService, times(1)).findCaseById(CASE_ID, securityDto);
    }

    @Test
    public void shouldUpdateCaseWhenExistingCase() {
        GrantOfRepresentationData caseData = new GrantOfRepresentationData();
        caseData.setPrimaryApplicantEmailAddress(EMAIL_ADDRESS);
        CaseInfo caseInfo = new CaseInfo();
        caseInfo.setCaseId(CASE_ID);
        caseInfo.setState(CaseState.DRAFT);
        ProbateCaseDetails caseRequest = ProbateCaseDetails.builder().caseData(caseData).caseInfo(caseInfo).build();
        SecurityDto securityDto = SecurityDto.builder().build();
        Optional<ProbateCaseDetails> caseResponseOptional = Optional.of(caseRequest);
        when(securityUtils.getSecurityDto()).thenReturn(securityDto);
        when(coreCaseDataService.findCase(EMAIL_ADDRESS, GRANT_OF_REPRESENTATION, securityDto))
            .thenReturn(caseResponseOptional);
        when(coreCaseDataService.updateCase(CASE_ID, caseData, UPDATE_DRAFT, securityDto)).thenReturn(caseRequest);

        ProbateCaseDetails caseResponse = casesService.saveCase(EMAIL_ADDRESS, caseRequest);

        assertThat(caseResponse.getCaseData(), is(caseData));
        verify(securityUtils, times(1)).getSecurityDto();
        verify(coreCaseDataService, times(1)).findCase(EMAIL_ADDRESS, GRANT_OF_REPRESENTATION, securityDto);
        verify(coreCaseDataService, times(1)).updateCase(CASE_ID, caseData, UPDATE_DRAFT, securityDto);
    }

    @Test
    public void shouldUpdateCaseAsCaseworkerWhenExistingCase() {
        GrantOfRepresentationData caseData = new GrantOfRepresentationData();
        caseData.setPrimaryApplicantEmailAddress(EMAIL_ADDRESS);
        CaseInfo caseInfo = new CaseInfo();
        caseInfo.setCaseId(CASE_ID);
        caseInfo.setState(CaseState.DRAFT);
        ProbateCaseDetails caseRequest = ProbateCaseDetails.builder().caseData(caseData).caseInfo(caseInfo).build();
        SecurityDto securityDto = SecurityDto.builder().build();
        Optional<ProbateCaseDetails> caseResponseOptional = Optional.of(caseRequest);
        when(securityUtils.getSecurityDto()).thenReturn(securityDto);
        when(coreCaseDataService.findCase(EMAIL_ADDRESS, GRANT_OF_REPRESENTATION, securityDto))
            .thenReturn(caseResponseOptional);
        when(coreCaseDataService.updateCaseAsCaseworker(CASE_ID, caseData, UPDATE_DRAFT, securityDto))
            .thenReturn(caseRequest);

        ProbateCaseDetails caseResponse = casesService.saveCaseAsCaseworker(EMAIL_ADDRESS, caseRequest);

        assertThat(caseResponse.getCaseData(), is(caseData));
        verify(securityUtils, times(1)).getSecurityDto();
        verify(coreCaseDataService, times(1)).findCase(EMAIL_ADDRESS, GRANT_OF_REPRESENTATION, securityDto);
        verify(coreCaseDataService, times(1)).updateCaseAsCaseworker(CASE_ID, caseData, UPDATE_DRAFT, securityDto);
    }

    @Test
    public void shouldCreateNewCaseWhenNoneExisting() {
        GrantOfRepresentationData caseData = new GrantOfRepresentationData();
        caseData.setPrimaryApplicantEmailAddress(EMAIL_ADDRESS);
        CaseInfo caseInfo = new CaseInfo();
        caseInfo.setCaseId(CASE_ID);
        caseInfo.setState(CaseState.DRAFT);
        ProbateCaseDetails caseRequest = ProbateCaseDetails.builder().caseData(caseData).caseInfo(caseInfo).build();
        SecurityDto securityDto = SecurityDto.builder().build();
        when(securityUtils.getSecurityDto()).thenReturn(securityDto);
        when(coreCaseDataService.findCase(EMAIL_ADDRESS, GRANT_OF_REPRESENTATION, securityDto))
            .thenReturn(Optional.empty());
        when(coreCaseDataService.createCase(caseData, CREATE_DRAFT, securityDto)).thenReturn(caseRequest);

        ProbateCaseDetails caseResponse = casesService.saveCase(EMAIL_ADDRESS, caseRequest);

        assertThat(caseResponse.getCaseData(), is(caseData));
        verify(securityUtils, times(1)).getSecurityDto();
        verify(coreCaseDataService, times(1)).findCase(EMAIL_ADDRESS, GRANT_OF_REPRESENTATION, securityDto);
        verify(coreCaseDataService, times(1)).createCase(caseData, CREATE_DRAFT, securityDto);
    }

    @Test
    public void shouldInitiateCase() {
        GrantOfRepresentationData caseData = new GrantOfRepresentationData();
        CaseType caseType = CaseType.getCaseType(caseData);

        ProbateCaseDetails caseRequest = ProbateCaseDetails.builder().caseData(caseData).build();
        SecurityDto securityDto = SecurityDto.builder().build();
        when(securityUtils.getSecurityDto()).thenReturn(securityDto);
        when(coreCaseDataService.createCase(caseData, CREATE_DRAFT, securityDto))
            .thenReturn(caseRequest);

        ProbateCaseDetails caseResponse = casesService.initiateCase(caseRequest);

        assertThat(caseResponse.getCaseData(), is(caseData));
        verify(securityUtils, times(1)).getSecurityDto();
        verify(coreCaseDataService, times(1)).createCase(caseData, CREATE_DRAFT, securityDto);
    }
}
