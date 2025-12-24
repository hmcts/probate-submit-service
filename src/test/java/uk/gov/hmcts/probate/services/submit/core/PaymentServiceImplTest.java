package uk.gov.hmcts.probate.services.submit.core;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.probate.security.SecurityDto;
import uk.gov.hmcts.probate.security.SecurityUtils;
import uk.gov.hmcts.probate.services.submit.model.v2.exception.CaseNotFoundException;
import uk.gov.hmcts.probate.services.submit.model.v2.exception.CaseStatePreconditionException;
import uk.gov.hmcts.probate.services.submit.services.CoreCaseDataService;
import uk.gov.hmcts.probate.services.submit.services.FeatureToggleService;
import uk.gov.hmcts.probate.services.submit.services.ValidationService;
import uk.gov.hmcts.reform.probate.model.PaymentStatus;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CaseEvents;
import uk.gov.hmcts.reform.probate.model.cases.CaseInfo;
import uk.gov.hmcts.reform.probate.model.cases.CasePayment;
import uk.gov.hmcts.reform.probate.model.cases.CaseState;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.CollectionMember;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantOfRepresentationData;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

public class PaymentServiceImplTest {

    private static final String CASE_ID = "12323213323";
    private static final CaseState STATE = CaseState.PA_APP_CREATED;
    private static final String APPLICANT_EMAIL = "test@test.com";
    private static final String EVENT_DESCRIPTION = "update case with payment details";

    @Mock
    CoreCaseDataService mockCoreCaseDataService;
    @Mock
    SecurityUtils mockSecurityUtils;
    @Mock
    EventFactory eventFactory;
    @Mock
    RegistryService registryService;
    @Mock
    ValidationService validationService;
    @Mock
    FeatureToggleService featureToggleServiceMock;

    PaymentServiceImpl paymentService;

    AutoCloseable closeableMocks;

    // test data below
    private CaseData caseData;

    private SecurityDto securityDto;

    private CaseInfo caseInfo;

    private ProbateCaseDetails caseResponse;

    private CasePayment payment;

    private ProbateCaseDetails probateCaseDetailsRequest;

    @BeforeEach
    public void setUp() {
        closeableMocks = MockitoAnnotations.openMocks(this);
        paymentService = new PaymentServiceImpl(
                mockCoreCaseDataService,
                mockSecurityUtils,
                eventFactory,
                registryService,
                validationService,
                featureToggleServiceMock);

        when(featureToggleServiceMock.useCcdLookupForPayments())
            .thenReturn(false);


        securityDto = mock(SecurityDto.class);

        payment = new CasePayment();
        payment.setSiteId("site-id-123");
        payment.setTransactionId("XXXXXX1234");
        payment.setMethod("online");
        payment.setReference("REFERENCE00000");
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setDate(Date.from(LocalDate.of(2018, 1, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        payment.setAmount(100000L);

        caseData = new GrantOfRepresentationData();
        caseData.setPayments(Lists.newArrayList(CollectionMember.<CasePayment>builder()
            .value(payment)
            .build()));

        caseInfo = new CaseInfo();
        caseInfo.setCaseId(CASE_ID);
        caseInfo.setState(STATE);

        probateCaseDetailsRequest = ProbateCaseDetails.builder().caseData(caseData).caseInfo(caseInfo).build();

        caseResponse = ProbateCaseDetails.builder().caseData(caseData).caseInfo(caseInfo).build();

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

    @AfterEach
    public void tearDown() throws Exception {
        closeableMocks.close();
    }

    @Test
    public void shouldUpdateCaseByCaseIdAndIsSuccess() {
        when(mockSecurityUtils.getSecurityDto()).thenReturn(securityDto);
        when(mockCoreCaseDataService.findCaseById(CASE_ID, securityDto))
            .thenReturn(Optional.of(caseResponse));
        when(mockCoreCaseDataService.updateCaseAsCaseworker(eq(CASE_ID), eq(caseData),
            eq(GOP_CREATE_CASE), eq(securityDto)))
            .thenReturn(caseResponse);

        ProbateCaseDetails actualCaseResponse = paymentService.updateCaseByCaseId(CASE_ID, probateCaseDetailsRequest);

        assertEquals(caseResponse, actualCaseResponse);
        verify(mockSecurityUtils).getSecurityDto();
        verify(mockCoreCaseDataService).findCaseById(CASE_ID, securityDto);
        verify(mockCoreCaseDataService).updateCaseAsCaseworker(eq(CASE_ID), eq(caseData),
            eq(GOP_CREATE_CASE), eq(securityDto));
        verify(registryService).updateRegistry(eq(caseData));
    }

    @Test
    public void shouldNotUpdatePaymentByCaseIdWhenCaseStateIsCaseCreated() {
        caseResponse.getCaseInfo().setState(CaseState.CASE_CREATED);
        when(mockSecurityUtils.getSecurityDto()).thenReturn(securityDto);
        when(mockCoreCaseDataService.findCaseById(CASE_ID, securityDto))
            .thenReturn(Optional.of(caseResponse));

        ProbateCaseDetails actualCaseResponse = paymentService.updateCaseByCaseId(CASE_ID, probateCaseDetailsRequest);

        assertEquals(caseResponse, actualCaseResponse);
        verify(mockSecurityUtils).getSecurityDto();
        verify(mockCoreCaseDataService).findCaseById(CASE_ID, securityDto);
        verify(mockCoreCaseDataService, never()).updateCase(eq(CASE_ID), eq(caseData),
            eq(GOP_CREATE_CASE), eq(securityDto), eq(EVENT_DESCRIPTION));
    }

    @Test
    public void shouldCreateCaseWhenPaymentStatusIsSuccessWithFTOff() {
        when(mockSecurityUtils.getSecurityDto()).thenReturn(securityDto);
        when(mockCoreCaseDataService.findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDto))
                .thenReturn(Optional.of(caseResponse));
        when(mockCoreCaseDataService.updateCase(eq(CASE_ID), eq(caseData),
                eq(GOP_CREATE_CASE), eq(securityDto), eq(EVENT_DESCRIPTION)))
                .thenReturn(caseResponse);

        ProbateCaseDetails actualCaseResponse = paymentService.createCase(APPLICANT_EMAIL, caseResponse);

        assertEquals(caseResponse, actualCaseResponse);
        verify(mockSecurityUtils).getSecurityDto();
        verify(mockCoreCaseDataService).findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDto);
        verify(mockCoreCaseDataService).updateCase(eq(CASE_ID), eq(caseData),
                eq(GOP_CREATE_CASE), eq(securityDto), eq(EVENT_DESCRIPTION));
    }

    @Test
    public void shouldCreateCaseWhenPaymentStatusIsSuccessWithFTOn() {
        when(featureToggleServiceMock.useCcdLookupForPayments())
                .thenReturn(true);
        when(mockSecurityUtils.getSecurityDto()).thenReturn(securityDto);
        when(mockCoreCaseDataService.findCaseById(APPLICANT_EMAIL, securityDto))
                .thenReturn(Optional.of(caseResponse));
        when(mockCoreCaseDataService.updateCase(eq(CASE_ID), eq(caseData),
                eq(GOP_CREATE_CASE), eq(securityDto), eq(EVENT_DESCRIPTION)))
                .thenReturn(caseResponse);

        ProbateCaseDetails actualCaseResponse = paymentService.createCase(APPLICANT_EMAIL, caseResponse);

        assertEquals(caseResponse, actualCaseResponse);
        verify(mockSecurityUtils).getSecurityDto();
        verify(mockCoreCaseDataService).findCaseById(APPLICANT_EMAIL, securityDto);
        verify(mockCoreCaseDataService).updateCase(eq(CASE_ID), eq(caseData),
                eq(GOP_CREATE_CASE), eq(securityDto), eq(EVENT_DESCRIPTION));
    }

    @Test
    public void shouldCreateCaseWhenPaymentStatusIsFailedWithFTOff() {
        caseData.getPayments().get(0).getValue().setStatus(PaymentStatus.FAILED);
        when(mockSecurityUtils.getSecurityDto()).thenReturn(securityDto);
        when(mockCoreCaseDataService.findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDto))
                .thenReturn(Optional.of(caseResponse));
        when(mockCoreCaseDataService.updateCase(eq(CASE_ID), eq(caseData),
                eq(GOP_PAYMENT_FAILED), eq(securityDto), eq(EVENT_DESCRIPTION)))
                .thenReturn(caseResponse);

        ProbateCaseDetails actualCaseResponse = paymentService.createCase(APPLICANT_EMAIL, caseResponse);

        assertEquals(caseResponse, actualCaseResponse);
        verify(mockSecurityUtils).getSecurityDto();
        verify(mockCoreCaseDataService).findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDto);
        verify(mockCoreCaseDataService).updateCase(eq(CASE_ID), eq(caseData),
                eq(GOP_PAYMENT_FAILED), eq(securityDto), eq(EVENT_DESCRIPTION));
    }

    @Test
    public void shouldCreateCaseWhenPaymentStatusIsFailedWithFTOn() {
        when(featureToggleServiceMock.useCcdLookupForPayments())
                .thenReturn(true);
        caseData.getPayments().get(0).getValue().setStatus(PaymentStatus.FAILED);
        when(mockSecurityUtils.getSecurityDto()).thenReturn(securityDto);
        when(mockCoreCaseDataService.findCaseById(APPLICANT_EMAIL, securityDto))
                .thenReturn(Optional.of(caseResponse));
        when(mockCoreCaseDataService.updateCase(eq(CASE_ID), eq(caseData),
                eq(GOP_PAYMENT_FAILED), eq(securityDto), eq(EVENT_DESCRIPTION)))
                .thenReturn(caseResponse);

        ProbateCaseDetails actualCaseResponse = paymentService.createCase(APPLICANT_EMAIL, caseResponse);

        assertEquals(caseResponse, actualCaseResponse);
        verify(mockSecurityUtils).getSecurityDto();
        verify(mockCoreCaseDataService).findCaseById(APPLICANT_EMAIL, securityDto);
        verify(mockCoreCaseDataService).updateCase(eq(CASE_ID), eq(caseData),
                eq(GOP_PAYMENT_FAILED), eq(securityDto), eq(EVENT_DESCRIPTION));
    }

    @Test
    public void shouldCreateCaseWhenPaymentStatusIsFailedAgainWithFTOff() {
        caseInfo.setState(CaseState.CASE_PAYMENT_FAILED);
        caseData.getPayments().get(0).getValue().setStatus(PaymentStatus.FAILED);
        when(mockSecurityUtils.getSecurityDto()).thenReturn(securityDto);
        when(mockCoreCaseDataService.findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDto))
                .thenReturn(Optional.of(caseResponse));
        when(mockCoreCaseDataService.updateCase(eq(CASE_ID), eq(caseData),
                eq(GOP_PAYMENT_FAILED_AGAIN), eq(securityDto), eq(EVENT_DESCRIPTION)))
                .thenReturn(caseResponse);

        ProbateCaseDetails actualCaseResponse = paymentService.createCase(APPLICANT_EMAIL, caseResponse);

        assertEquals(caseResponse, actualCaseResponse);
        verify(mockSecurityUtils).getSecurityDto();
        verify(mockCoreCaseDataService).findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDto);
        verify(mockCoreCaseDataService).updateCase(eq(CASE_ID), eq(caseData),
                eq(GOP_PAYMENT_FAILED_AGAIN), eq(securityDto), eq(EVENT_DESCRIPTION));
    }

    @Test
    public void shouldCreateCaseWhenPaymentStatusIsFailedAgainWithFTOn() {
        when(featureToggleServiceMock.useCcdLookupForPayments())
                .thenReturn(true);
        caseInfo.setState(CaseState.CASE_PAYMENT_FAILED);
        caseData.getPayments().get(0).getValue().setStatus(PaymentStatus.FAILED);
        when(mockSecurityUtils.getSecurityDto()).thenReturn(securityDto);
        when(mockCoreCaseDataService.findCaseById(APPLICANT_EMAIL, securityDto))
                .thenReturn(Optional.of(caseResponse));
        when(mockCoreCaseDataService.updateCase(eq(CASE_ID), eq(caseData),
                eq(GOP_PAYMENT_FAILED_AGAIN), eq(securityDto), eq(EVENT_DESCRIPTION)))
                .thenReturn(caseResponse);

        ProbateCaseDetails actualCaseResponse = paymentService.createCase(APPLICANT_EMAIL, caseResponse);

        assertEquals(caseResponse, actualCaseResponse);
        verify(mockSecurityUtils).getSecurityDto();
        verify(mockCoreCaseDataService).findCaseById(APPLICANT_EMAIL, securityDto);
        verify(mockCoreCaseDataService).updateCase(eq(CASE_ID), eq(caseData),
                eq(GOP_PAYMENT_FAILED_AGAIN), eq(securityDto), eq(EVENT_DESCRIPTION));
    }

    @Test
    public void shouldCreateCaseWhenPaymentStatusIsSuccessAfterFailureWithFTOff() {
        caseInfo.setState(CaseState.CASE_PAYMENT_FAILED);
        caseData.getPayments().get(0).getValue().setStatus(PaymentStatus.SUCCESS);
        when(mockSecurityUtils.getSecurityDto()).thenReturn(securityDto);
        when(mockCoreCaseDataService.findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDto))
                .thenReturn(Optional.of(caseResponse));
        when(mockCoreCaseDataService.updateCase(eq(CASE_ID), eq(caseData),
                eq(GOP_PAYMENT_FAILED_TO_SUCCESS), eq(securityDto), eq(EVENT_DESCRIPTION)))
                .thenReturn(caseResponse);

        ProbateCaseDetails actualCaseResponse = paymentService.createCase(APPLICANT_EMAIL, caseResponse);

        assertEquals(caseResponse, actualCaseResponse);
        verify(mockSecurityUtils).getSecurityDto();
        verify(mockCoreCaseDataService).findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDto);
        verify(mockCoreCaseDataService).updateCase(eq(CASE_ID), eq(caseData),
                eq(GOP_PAYMENT_FAILED_TO_SUCCESS), eq(securityDto), eq(EVENT_DESCRIPTION));
    }

    @Test
    public void shouldCreateCaseWhenPaymentStatusIsSuccessAfterFailureWithFTOn() {
        when(featureToggleServiceMock.useCcdLookupForPayments())
                .thenReturn(true);
        caseInfo.setState(CaseState.CASE_PAYMENT_FAILED);
        caseData.getPayments().get(0).getValue().setStatus(PaymentStatus.SUCCESS);
        when(mockSecurityUtils.getSecurityDto()).thenReturn(securityDto);
        when(mockCoreCaseDataService.findCaseById(APPLICANT_EMAIL, securityDto))
                .thenReturn(Optional.of(caseResponse));
        when(mockCoreCaseDataService.updateCase(eq(CASE_ID), eq(caseData),
                eq(GOP_PAYMENT_FAILED_TO_SUCCESS), eq(securityDto), eq(EVENT_DESCRIPTION)))
                .thenReturn(caseResponse);

        ProbateCaseDetails actualCaseResponse = paymentService.createCase(APPLICANT_EMAIL, caseResponse);

        assertEquals(caseResponse, actualCaseResponse);
        verify(mockSecurityUtils).getSecurityDto();
        verify(mockCoreCaseDataService).findCaseById(APPLICANT_EMAIL, securityDto);
        verify(mockCoreCaseDataService).updateCase(eq(CASE_ID), eq(caseData),
                eq(GOP_PAYMENT_FAILED_TO_SUCCESS), eq(securityDto), eq(EVENT_DESCRIPTION));
    }

    @Test
    void shouldThrowCaseNotFoundException() {
        when(mockSecurityUtils.getSecurityDto()).thenReturn(securityDto);
        when(mockCoreCaseDataService.findCaseById(CASE_ID, securityDto))
                .thenReturn(Optional.of(caseResponse));

        assertThrows(CaseNotFoundException.class, () -> {
            paymentService.updateCaseByCaseId(null, probateCaseDetailsRequest);
        });

        verify(mockSecurityUtils).getSecurityDto();

    }

    @Test
    void shouldThrowCaseStatePreconditionExceptionWithFTOff() {
        caseResponse.getCaseInfo().setState(null);
        when(mockSecurityUtils.getSecurityDto()).thenReturn(securityDto);
        when(mockCoreCaseDataService.findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDto))
                .thenReturn(Optional.of(caseResponse));

        CaseStatePreconditionException exception = assertThrows(CaseStatePreconditionException.class, () -> {
            paymentService.createCase(APPLICANT_EMAIL, caseResponse);
        });

        assertEquals("Event ID not present for case state: null and payment status: SUCCESS combination",
                exception.getMessage());
        verify(mockSecurityUtils).getSecurityDto();
        verify(mockCoreCaseDataService).findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDto);
    }

    @Test
    void shouldThrowCaseStatePreconditionExceptionWithFTOn() {
        when(featureToggleServiceMock.useCcdLookupForPayments())
                .thenReturn(true);
        caseResponse.getCaseInfo().setState(null);
        when(mockSecurityUtils.getSecurityDto()).thenReturn(securityDto);
        when(mockCoreCaseDataService.findCaseById(APPLICANT_EMAIL, securityDto))
                .thenReturn(Optional.of(caseResponse));

        CaseStatePreconditionException exception = assertThrows(CaseStatePreconditionException.class, () -> {
            paymentService.createCase(APPLICANT_EMAIL, caseResponse);
        });

        assertEquals("Event ID not present for case state: null and payment status: SUCCESS combination",
                exception.getMessage());
        verify(mockSecurityUtils).getSecurityDto();
        verify(mockCoreCaseDataService).findCaseById(APPLICANT_EMAIL, securityDto);
    }


}
