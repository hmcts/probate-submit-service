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
import uk.gov.hmcts.probate.services.submit.model.v2.exception.CaseNotFoundException;
import uk.gov.hmcts.probate.services.submit.model.v2.exception.CaseStatePreconditionException;
import uk.gov.hmcts.probate.services.submit.services.CoreCaseDataService;
import uk.gov.hmcts.reform.probate.model.PaymentStatus;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CaseEvents;
import uk.gov.hmcts.reform.probate.model.cases.CaseInfo;
import uk.gov.hmcts.reform.probate.model.cases.CasePayment;
import uk.gov.hmcts.reform.probate.model.cases.CaseState;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.ProbatePaymentDetails;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantOfRepresentationData;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
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
public class PaymentServiceImplTest {

    private static final String CASE_ID = "12323213323";
    private static final String STATE = CaseState.PA_APP_CREATED.getName();
    private static final String APPLICANT_EMAIL = "test@test.com";

    @Mock
    private CoreCaseDataService mockCoreCaseDataService;

    @Mock
    private SecurityUtils mockSecurityUtils;

    @Mock
    private EventFactory eventFactory;

    @Mock
    private SearchFieldFactory searchFieldFactory;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private CaseData caseData;

    private SecurityDTO securityDTO;

    private CaseInfo caseInfo;

    private ProbateCaseDetails caseResponse;

    private CasePayment payment;

    private ProbatePaymentDetails paymentUpdateRequest;

    @Before
    public void setUp() {
        payment = new CasePayment();
        payment.setSiteId("site-id-123");
        payment.setTransactionId("XXXXXX1234");
        payment.setMethod("online");
        payment.setReference("REFERENCE00000");
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setDate(Date.from(LocalDate.of(2018, 1, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        payment.setAmount(100000L);
        paymentUpdateRequest = ProbatePaymentDetails.builder().caseType(CaseType.GRANT_OF_REPRESENTATION)
                .payment(payment)
                .build();
        caseData = new GrantOfRepresentationData();
        caseInfo = new CaseInfo();
        caseInfo.setCaseId(CASE_ID);
        caseInfo.setState(STATE);
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

    @Test
    public void shouldAddPaymentToCaseWhenPaymentStatusIsSuccess() {
        when(mockSecurityUtils.getSecurityDTO()).thenReturn(securityDTO);
        when(mockCoreCaseDataService.findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDTO))
                .thenReturn(Optional.of(caseResponse));
        when(mockCoreCaseDataService.updateCase(eq(CASE_ID), eq(caseData),
                eq(GOP_CREATE_CASE), eq(securityDTO)))
                .thenReturn(caseResponse);

        ProbateCaseDetails actualCaseResponse = paymentService.addPaymentToCase(APPLICANT_EMAIL, paymentUpdateRequest);

        assertThat(actualCaseResponse, equalTo(actualCaseResponse));
        verify(mockSecurityUtils).getSecurityDTO();
        verify(mockCoreCaseDataService).findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDTO);
        verify(mockCoreCaseDataService).updateCase(eq(CASE_ID), eq(caseData),
                eq(GOP_CREATE_CASE), eq(securityDTO));
    }

    @Test
    public void shouldAddPaymentToCaseWhenPaymentStatusIsFailed() {
        when(mockSecurityUtils.getSecurityDTO()).thenReturn(securityDTO);
        when(mockCoreCaseDataService.findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDTO))
                .thenReturn(Optional.of(caseResponse));
        when(mockCoreCaseDataService.updateCase(eq(CASE_ID), eq(caseData),
                eq(GOP_PAYMENT_FAILED), eq(securityDTO)))
                .thenReturn(caseResponse);
        paymentUpdateRequest.getPayment().setStatus(PaymentStatus.FAILED);

        ProbateCaseDetails actualCaseResponse = paymentService.addPaymentToCase(APPLICANT_EMAIL, paymentUpdateRequest);

        assertThat(actualCaseResponse, equalTo(actualCaseResponse));
        verify(mockSecurityUtils).getSecurityDTO();
        verify(mockCoreCaseDataService).findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDTO);
        verify(mockCoreCaseDataService).updateCase(eq(CASE_ID), eq(caseData),
                eq(GOP_PAYMENT_FAILED), eq(securityDTO));
    }

    @Test
    public void shouldAddPaymentToCaseWhenPaymentStatusIsFailedAgain() {
        caseInfo.setState(CaseState.CASE_PAYMENT_FAILED.getName());
        when(mockSecurityUtils.getSecurityDTO()).thenReturn(securityDTO);
        when(mockCoreCaseDataService.findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDTO))
                .thenReturn(Optional.of(caseResponse));
        when(mockCoreCaseDataService.updateCase(eq(CASE_ID), eq(caseData),
                eq(GOP_PAYMENT_FAILED_AGAIN), eq(securityDTO)))
                .thenReturn(caseResponse);
        paymentUpdateRequest.getPayment().setStatus(PaymentStatus.FAILED);

        ProbateCaseDetails actualCaseResponse = paymentService.addPaymentToCase(APPLICANT_EMAIL, paymentUpdateRequest);

        assertThat(actualCaseResponse, equalTo(actualCaseResponse));
        verify(mockSecurityUtils).getSecurityDTO();
        verify(mockCoreCaseDataService).findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDTO);
        verify(mockCoreCaseDataService).updateCase(eq(CASE_ID), eq(caseData),
                eq(GOP_PAYMENT_FAILED_AGAIN), eq(securityDTO));
    }

    @Test
    public void shouldAddPaymentToCaseWhenPaymentStatusIsSuccessAfterFailure() {
        caseInfo.setState(CaseState.CASE_PAYMENT_FAILED.getName());
        when(mockSecurityUtils.getSecurityDTO()).thenReturn(securityDTO);
        when(mockCoreCaseDataService.findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDTO))
                .thenReturn(Optional.of(caseResponse));
        when(mockCoreCaseDataService.updateCase(eq(CASE_ID), eq(caseData),
                eq(GOP_PAYMENT_FAILED_TO_SUCCESS), eq(securityDTO)))
                .thenReturn(caseResponse);
        paymentUpdateRequest.getPayment().setStatus(PaymentStatus.SUCCESS);

        ProbateCaseDetails actualCaseResponse = paymentService.addPaymentToCase(APPLICANT_EMAIL, paymentUpdateRequest);

        assertThat(actualCaseResponse, equalTo(actualCaseResponse));
        verify(mockSecurityUtils).getSecurityDTO();
        verify(mockCoreCaseDataService).findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDTO);
        verify(mockCoreCaseDataService).updateCase(eq(CASE_ID), eq(caseData),
                eq(GOP_PAYMENT_FAILED_TO_SUCCESS), eq(securityDTO));
    }

    @Test(expected = CaseStatePreconditionException.class)
    public void shouldCasePreconditionExceptionIfInvalidStateForPayment() {
        caseInfo.setState(CaseState.DRAFT.getName());
        when(mockSecurityUtils.getSecurityDTO()).thenReturn(securityDTO);
        when(mockCoreCaseDataService.findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDTO))
                .thenReturn(Optional.of(caseResponse));
        paymentUpdateRequest.getPayment().setStatus(PaymentStatus.SUCCESS);

        paymentService.addPaymentToCase(APPLICANT_EMAIL, paymentUpdateRequest);
    }

    @Test(expected = CaseNotFoundException.class)
    public void shouldThrowCaseNotFoundExceptionWhenNoExistingCase() {
        when(mockSecurityUtils.getSecurityDTO()).thenReturn(securityDTO);
        when(mockCoreCaseDataService.findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDTO))
                .thenReturn(Optional.empty());

        paymentService.addPaymentToCase(APPLICANT_EMAIL, paymentUpdateRequest);
    }
}