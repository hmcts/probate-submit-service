package uk.gov.hmcts.probate.services.submit.core;

import com.google.common.collect.ImmutableMap;
import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.tuple.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.probate.security.SecurityDto;
import uk.gov.hmcts.probate.security.SecurityUtils;
import uk.gov.hmcts.probate.services.submit.model.v2.exception.CaseNotFoundException;
import uk.gov.hmcts.probate.services.submit.model.v2.exception.CaseStatePreconditionException;
import uk.gov.hmcts.probate.services.submit.services.CoreCaseDataService;
import uk.gov.hmcts.probate.services.submit.services.PaymentsService;
import uk.gov.hmcts.probate.services.submit.services.ValidationService;
import uk.gov.hmcts.reform.probate.model.PaymentStatus;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CaseEvents;
import uk.gov.hmcts.reform.probate.model.cases.CasePayment;
import uk.gov.hmcts.reform.probate.model.cases.CaseState;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.CollectionMember;
import uk.gov.hmcts.reform.probate.model.cases.EventId;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static uk.gov.hmcts.reform.probate.model.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.probate.model.PaymentStatus.INITIATED;
import static uk.gov.hmcts.reform.probate.model.PaymentStatus.NOT_REQUIRED;
import static uk.gov.hmcts.reform.probate.model.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.probate.model.cases.CaseState.CASE_PAYMENT_FAILED;
import static uk.gov.hmcts.reform.probate.model.cases.CaseState.DRAFT;
import static uk.gov.hmcts.reform.probate.model.cases.CaseState.PA_APP_CREATED;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentsService {

    private static final Map<Pair<CaseState, PaymentStatus>, Function<CaseEvents, EventId>> PAYMENT_EVENT_MAP =
        ImmutableMap.<Pair<CaseState, PaymentStatus>, Function<CaseEvents, EventId>>builder()
            .put(Pair.of(DRAFT, INITIATED), CaseEvents::getCreateCaseApplicationEventId)
            .put(Pair.of(DRAFT, NOT_REQUIRED), CaseEvents::getCreateCaseWithoutPaymentId)
            .put(Pair.of(PA_APP_CREATED, SUCCESS), CaseEvents::getCreateCaseEventId)
            .put(Pair.of(PA_APP_CREATED, FAILED), CaseEvents::getPaymentFailedEventId)
            .put(Pair.of(PA_APP_CREATED, INITIATED), CaseEvents::getUpdateCaseApplicationEventId)
            .put(Pair.of(PA_APP_CREATED, null), CaseEvents::getPaymentFailedEventId)
            .put(Pair.of(CASE_PAYMENT_FAILED, SUCCESS), CaseEvents::getPaymentFailedToSuccessEventId)
            .put(Pair.of(CASE_PAYMENT_FAILED, FAILED), CaseEvents::getPaymentFailedAgainEventId)
            .put(Pair.of(CASE_PAYMENT_FAILED, INITIATED), CaseEvents::getUpdatePaymentFailedEventId)
            .put(Pair.of(CASE_PAYMENT_FAILED, null), CaseEvents::getPaymentFailedAgainEventId)
            .build();

    private final CoreCaseDataService coreCaseDataService;

    private final SecurityUtils securityUtils;

    private final EventFactory eventFactory;

    private final RegistryService registryService;

    private final ValidationService validationService;


    @Override
    public ProbateCaseDetails createCase(String searchField, ProbateCaseDetails probateCaseDetails) {
        CaseType caseType = CaseType.getCaseType(probateCaseDetails.getCaseData());
        log.info("Updating payment details for case type: {}", CaseType.getCaseType(probateCaseDetails.getCaseData()));
        SecurityDto securityDto = securityUtils.getSecurityDto();
        ProbateCaseDetails caseResponse = findCase(searchField, caseType, securityDto);

        validationService.validateForSubmission(probateCaseDetails);

        log.info("Found case with case Id: {}", caseResponse.getCaseInfo().getCaseId());
        String caseId = caseResponse.getCaseInfo().getCaseId();
        return updateCase(caseId, securityDto, caseType, probateCaseDetails);
    }

    private ProbateCaseDetails updateCase(String caseId, SecurityDto securityDto, CaseType caseType,
                                          ProbateCaseDetails probateCaseDetails) {
        CasePayment payment = probateCaseDetails.getCaseData().getPayments().get(0).getValue();
        CaseState caseState = probateCaseDetails.getCaseInfo().getState();
        CaseEvents caseEvents = eventFactory.getCaseEvents(caseType);
        EventId eventId = getEventId(caseState, payment).apply(caseEvents);
        CaseData caseData = probateCaseDetails.getCaseData();
        registryService.updateRegistry(caseData);
        return coreCaseDataService.updateCase(caseId, caseData, eventId,
            securityDto, "update case with payment details");
    }

    private ProbateCaseDetails updateCase(String caseId, ProbateCaseDetails updateRequest,
                                          SecurityDto securityDto, CaseType caseType, ProbateCaseDetails caseResponse) {
        CaseState caseState = caseResponse.getCaseInfo().getState();
        log.info("Updating case with id: {} and state: {}", caseId, caseState);
        if (CaseState.CASE_CREATED.equals(caseState)) {
            return caseResponse;
        }
        CasePayment payment = updateRequest.getCaseData().getPayments().get(0).getValue();
        CaseEvents caseEvents = eventFactory.getCaseEvents(caseType);
        EventId eventId = getEventId(caseState, payment).apply(caseEvents);
        CaseData caseData = updateRequest.getCaseData();
        log.info("Updating registry");
        registryService.updateRegistry(caseData);
        log.info("Updating case as caseWorker with eventId:{}", eventId);
        return coreCaseDataService.updateCaseAsCaseworker(caseId, caseData, eventId, securityDto);
    }

    @Override
    public ProbateCaseDetails updateCaseByCaseId(String caseId, ProbateCaseDetails probateUpdateRequest) {
        log.info("Updating payment details for case with id: {}", caseId);
        SecurityDto securityDto = securityUtils.getSecurityDto();
        ProbateCaseDetails caseResponse = findCaseById(caseId, securityDto);
        CaseType caseType = CaseType.getCaseType(caseResponse.getCaseData());
        log.info("Found case with case Id: {}", caseResponse.getCaseInfo().getCaseId());
        return updateCase(caseId, probateUpdateRequest, securityDto, caseType, caseResponse);
    }

    private ProbateCaseDetails findCase(String applicantEmail, CaseType caseType, SecurityDto securityDto) {
        Optional<ProbateCaseDetails> caseResponseOptional = coreCaseDataService
            .findCase(applicantEmail, caseType, securityDto);
        return caseResponseOptional.orElseThrow(CaseNotFoundException::new);
    }

    private ProbateCaseDetails findCaseById(String caseId, SecurityDto securityDto) {
        log.info("DEBUG: Finding case with Id: {}", caseId);
        Optional<ProbateCaseDetails> caseResponseOptional = coreCaseDataService
            .findCaseById(caseId, securityDto);
        return caseResponseOptional.orElseThrow(CaseNotFoundException::new);
    }

    private Function<CaseEvents, EventId> getEventId(CaseState caseState, CasePayment payment) {
        Optional<Function<CaseEvents, EventId>> optionalFunction =
            Optional.ofNullable(PAYMENT_EVENT_MAP.get(Pair.of(caseState, payment.getStatus())));
        return optionalFunction
            .orElseThrow(() -> new CaseStatePreconditionException(caseState, payment.getStatus()));
    }

    private CaseData createCaseData(ProbateCaseDetails caseResponse, CasePayment payment) {
        CaseData caseData = caseResponse.getCaseData();
        CollectionMember<CasePayment> collectionMember = new CollectionMember<CasePayment>();
        collectionMember.setValue(payment);
        caseData.setPayments(Arrays.asList(collectionMember));
        return caseData;
    }
}
