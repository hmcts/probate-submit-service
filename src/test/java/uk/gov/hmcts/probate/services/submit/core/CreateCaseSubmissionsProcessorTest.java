package uk.gov.hmcts.probate.services.submit.core;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.probate.security.SecurityDTO;
import uk.gov.hmcts.probate.security.SecurityUtils;
import uk.gov.hmcts.probate.services.submit.Registry;
import uk.gov.hmcts.probate.services.submit.core.proccessors.impl.CreateCaseSubmissionsProcessor;
import uk.gov.hmcts.probate.services.submit.model.v2.exception.CaseAlreadyExistsException;
import uk.gov.hmcts.probate.services.submit.services.CoreCaseDataService;
import uk.gov.hmcts.probate.services.submit.services.SequenceService;
import uk.gov.hmcts.probate.services.submit.validation.CaseDataValidatorFactory;
import uk.gov.hmcts.reform.probate.model.cases.CaseEvents;
import uk.gov.hmcts.reform.probate.model.cases.CaseInfo;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.RegistryLocation;
import uk.gov.hmcts.reform.probate.model.cases.SubmitResult;
import uk.gov.hmcts.reform.probate.model.cases.caveat.CaveatData;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.probate.model.cases.CaseType.CAVEAT;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_CREATE_APPLICATION;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_CREATE_CASE;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_PAYMENT_FAILED;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_PAYMENT_FAILED_AGAIN;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_PAYMENT_FAILED_TO_SUCCESS;

@RunWith(MockitoJUnitRunner.class)
public class CreateCaseSubmissionsProcessorTest {

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

    @Mock
    private SequenceService sequenceService;

    @Mock
    private Registry registry;

    private CreateCaseSubmissionsProcessor createCaseSubmissionsProcessor;

    private ProbateCaseDetails caseRequest;

    private CaveatData caseData;

    private SecurityDTO securityDTO;

    private CaseInfo caseInfo;

    private ProbateCaseDetails caseResponse;

    @Before
    public void setUp() {
        createCaseSubmissionsProcessor = new CreateCaseSubmissionsProcessor(
                coreCaseDataService, eventFactory, securityUtils, searchFieldFactory, caseDataValidatorFactory,
                sequenceService);
        securityDTO = SecurityDTO.builder().build();
        caseData = new CaveatData();
        caseData.setCaveatorEmailAddress(APPLICANT_EMAIL);
        caseRequest = ProbateCaseDetails.builder().caseData(caseData).build();
        caseInfo = new CaseInfo();
        caseInfo.setCaseId(CASE_ID);
        caseInfo.setState(STATE);
        caseResponse = ProbateCaseDetails.builder().caseData(caseData).caseInfo(caseInfo).build();

        when(searchFieldFactory.getSearchFieldValuePair(CaseType.CAVEAT, caseData))
                .thenReturn(ImmutablePair.of("caveatorEmailAddress", APPLICANT_EMAIL));

        when(eventFactory.getCaseEvents(CaseType.CAVEAT)).thenReturn(CaseEvents.builder()
                .createCaseApplicationEventId(GOP_CREATE_APPLICATION)
                .createCaseEventId(GOP_CREATE_CASE)
                .paymentFailedAgainEventId(GOP_PAYMENT_FAILED_AGAIN)
                .paymentFailedEventId(GOP_PAYMENT_FAILED)
                .paymentFailedToSuccessEventId(GOP_PAYMENT_FAILED_TO_SUCCESS)
                .build());
        when(registry.getName()).thenReturn(RegistryLocation.MANCHESTER.getName());
        when(sequenceService.identifyNextRegistry()).thenReturn(registry);
    }

    @Test
    public void shouldSubmitCaseWhenNoExistingCase() {
        when(securityUtils.getSecurityDTO()).thenReturn(securityDTO);
        when(coreCaseDataService.findCase(APPLICANT_EMAIL, CAVEAT, securityDTO))
                .thenReturn(Optional.empty());

        SubmitResult result = createCaseSubmissionsProcessor.process(APPLICANT_EMAIL, caseRequest);
        verify(coreCaseDataService, times(1)).findCase(APPLICANT_EMAIL, CAVEAT, securityDTO);
        verify(coreCaseDataService, times(1)).createCase(eq(caseData),
                eq(GOP_CREATE_APPLICATION), eq(securityDTO));
    }

    @Test(expected = CaseAlreadyExistsException.class)
    public void shouldNotSubmitWhenExistingCase() {
        when(securityUtils.getSecurityDTO()).thenReturn(securityDTO);
        when(coreCaseDataService.findCase(APPLICANT_EMAIL, CAVEAT, securityDTO))
                .thenReturn(Optional.of(caseResponse));

        createCaseSubmissionsProcessor.process(APPLICANT_EMAIL, caseRequest);
    }

}
