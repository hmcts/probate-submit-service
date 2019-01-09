package uk.gov.hmcts.probate.services.submit.core;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.probate.security.SecurityDTO;
import uk.gov.hmcts.probate.security.SecurityUtils;
import uk.gov.hmcts.probate.services.submit.clients.v2.ccd.CaseState;
import uk.gov.hmcts.probate.services.submit.clients.v2.ccd.EventId;
import uk.gov.hmcts.probate.services.submit.model.v2.exception.CaseNotFoundException;
import uk.gov.hmcts.probate.services.submit.model.v2.exception.CaseStatePreconditionException;
import uk.gov.hmcts.probate.services.submit.services.CoreCaseDataService;
import uk.gov.hmcts.probate.services.submit.services.PaymentsService;
import uk.gov.hmcts.reform.probate.model.PaymentStatus;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CasePayment;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.CollectionMember;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.ProbatePaymentDetails;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.probate.services.submit.clients.v2.ccd.CaseState.CASE_PAYMENT_FAILED;
import static uk.gov.hmcts.probate.services.submit.clients.v2.ccd.CaseState.PA_APP_CREATED;
import static uk.gov.hmcts.probate.services.submit.clients.v2.ccd.EventId.PAYMENT_FAILED;
import static uk.gov.hmcts.probate.services.submit.clients.v2.ccd.EventId.PAYMENT_FAILED_AGAIN;
import static uk.gov.hmcts.probate.services.submit.clients.v2.ccd.EventId.PAYMENT_FAILED_TO_SUCCESS;
import static uk.gov.hmcts.probate.services.submit.clients.v2.ccd.EventId.CREATE_CASE;
import static uk.gov.hmcts.reform.probate.model.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.probate.model.PaymentStatus.INITIATED;
import static uk.gov.hmcts.reform.probate.model.PaymentStatus.SUCCESS;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentsService {

    private final static Map<Pair<CaseState, PaymentStatus>, EventId> PAYMENT_EVENT_MAP =
            ImmutableMap.<Pair<CaseState, PaymentStatus>, EventId>builder()
                    .put(Pair.of(PA_APP_CREATED, SUCCESS), CREATE_CASE)
                    .put(Pair.of(PA_APP_CREATED, FAILED), PAYMENT_FAILED)
                    .put(Pair.of(PA_APP_CREATED, INITIATED), PAYMENT_FAILED)
                    .put(Pair.of(CASE_PAYMENT_FAILED, SUCCESS), PAYMENT_FAILED_TO_SUCCESS)
                    .put(Pair.of(CASE_PAYMENT_FAILED, FAILED), PAYMENT_FAILED_AGAIN)
                    .put(Pair.of(CASE_PAYMENT_FAILED, INITIATED), PAYMENT_FAILED_AGAIN)
                    .build();

    private final CoreCaseDataService coreCaseDataService;

    private final SecurityUtils securityUtils;

    @Override
    public ProbateCaseDetails addPaymentToCase(String applicantEmail, ProbatePaymentDetails paymentUpdateRequest) {
        log.info("Updating payment details for case type: {}", paymentUpdateRequest.getCaseType().getName());
        SecurityDTO securityDTO = securityUtils.getSecurityDTO();
        CaseType caseType = paymentUpdateRequest.getCaseType();
        ProbateCaseDetails caseResponse = findCase(applicantEmail, caseType, securityDTO);
        log.info("Found case with case Id: {}", caseResponse.getCaseInfo().getCaseId());
        String caseId = caseResponse.getCaseInfo().getCaseId();
        CaseState caseState = CaseState.getState(caseResponse.getCaseInfo().getState());
        CasePayment payment = paymentUpdateRequest.getPayment();
        EventId eventId = getEventId(caseState, payment);
        CaseData caseData = createCaseData(caseResponse, payment);
        return coreCaseDataService.updateCase(caseId, caseData, eventId, securityDTO);
    }

    private ProbateCaseDetails findCase(String applicantEmail, CaseType caseType, SecurityDTO securityDTO) {
        Optional<ProbateCaseDetails> caseResponseOptional = coreCaseDataService.
                findCase(applicantEmail, caseType, securityDTO);
        return caseResponseOptional.orElseThrow(CaseNotFoundException::new);
    }

    private EventId getEventId(CaseState caseState, CasePayment payment) {
        Optional<EventId> optionalEventId =
                Optional.ofNullable(PAYMENT_EVENT_MAP.get(Pair.of(caseState, payment.getStatus())));
        return optionalEventId
                .orElseThrow(() -> new CaseStatePreconditionException(caseState, payment.getStatus()));
    }

    private CaseData createCaseData(ProbateCaseDetails caseResponse, CasePayment payment) {
        CaseData caseData = caseResponse.getCaseData();
        CollectionMember collectionMember = new CollectionMember();
        collectionMember.setValue(payment);
        caseData.setPayments(Arrays.asList(collectionMember));
        return caseData;
    }
}
