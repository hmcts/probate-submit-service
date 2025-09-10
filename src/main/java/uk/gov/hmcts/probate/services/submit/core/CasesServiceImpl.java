package uk.gov.hmcts.probate.services.submit.core;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import uk.gov.hmcts.probate.security.SecurityDto;
import uk.gov.hmcts.probate.security.SecurityUtils;
import uk.gov.hmcts.probate.services.submit.model.v2.exception.CaseNotFoundException;
import uk.gov.hmcts.probate.services.submit.services.CasesService;
import uk.gov.hmcts.probate.services.submit.services.CoreCaseDataService;
import uk.gov.hmcts.probate.services.submit.services.ValidationService;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CaseEvents;
import uk.gov.hmcts.reform.probate.model.cases.CaseState;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.EventId;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantOfRepresentationData;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static uk.gov.hmcts.reform.probate.model.cases.CaseState.BO_CASE_STOPPED;
import static uk.gov.hmcts.reform.probate.model.cases.CaseState.CASE_PAYMENT_FAILED;
import static uk.gov.hmcts.reform.probate.model.cases.CaseState.DORMANT;
import static uk.gov.hmcts.reform.probate.model.cases.CaseState.DRAFT;
import static uk.gov.hmcts.reform.probate.model.cases.CaseState.PA_APP_CREATED;

@Slf4j
@Component
@RequiredArgsConstructor
public class CasesServiceImpl implements CasesService {

    private final CoreCaseDataService coreCaseDataService;

    private final SecurityUtils securityUtils;

    private final EventFactory eventFactory;

    private final SearchFieldFactory searchFieldFactory;

    private final ValidationService validationService;

    private final Map<CaseState, Function<CaseEvents, EventId>> eventMap =
        ImmutableMap.<CaseState, Function<CaseEvents, EventId>>builder()
            .put(DRAFT, CaseEvents::getUpdateDraftEventId)
            .put(PA_APP_CREATED, CaseEvents::getUpdateCaseApplicationEventId)
            .put(CASE_PAYMENT_FAILED, CaseEvents::getUpdatePaymentFailedEventId)
            .put(DORMANT, CaseEvents::getCitizenHubResponseDraftId)
            .put(BO_CASE_STOPPED, CaseEvents::getCitizenHubResponseDraftId)
            .build();

    @Override
    public ProbateCaseDetails getCase(String searchField, CaseType caseType) {
        log.info("Getting case of caseType: {}", caseType.getName());
        SecurityDto securityDto = securityUtils.getSecurityDto();
        Optional<ProbateCaseDetails> caseResponseOptional = coreCaseDataService
            .findCase(searchField, caseType, securityDto);
        return caseResponseOptional.orElseThrow(CaseNotFoundException::new);
    }

    @Override
    public ProbateCaseDetails getCaseByApplicantEmail(String searchField, CaseType caseType) {
        log.info("Getting case of caseType: {}", caseType.getName());
        SecurityDto securityDto = securityUtils.getSecurityDto();
        Optional<ProbateCaseDetails> caseResponseOptional = coreCaseDataService
            .findCaseByApplicantEmail(searchField, caseType, securityDto);
        return caseResponseOptional.orElseThrow(CaseNotFoundException::new);
    }

    @Override
    public List<ProbateCaseDetails> getAllCases(CaseType caseType) {
        log.info("Getting all cases of caseType: {}", caseType.getName());
        SecurityDto securityDto = securityUtils.getSecurityDto();
        return coreCaseDataService
            .findCases(caseType, securityDto);
    }

    @Override
    public ProbateCaseDetails getCaseById(String caseId) {
        log.info("Getting case by caseId: {}", caseId);
        SecurityDto securityDto = securityUtils.getSecurityDto();
        Optional<ProbateCaseDetails> caseResponseOptional = coreCaseDataService
            .findCaseById(caseId, securityDto);
        return caseResponseOptional.orElseThrow(CaseNotFoundException::new);
    }

    @Override
    public ProbateCaseDetails saveCase(String searchField, ProbateCaseDetails probateCaseDetails,
                                        String eventDescription) {
        log.info("saveDraft - Saving draft for case type: {}",
            probateCaseDetails.getCaseData().getClass().getSimpleName());
        return saveCase(searchField, probateCaseDetails, Boolean.FALSE, eventDescription);
    }

    private ProbateCaseDetails saveCase(String searchField, ProbateCaseDetails probateCaseDetails,
                                        Boolean asCaseworker, String eventDescription) {
        log.info("saveDraft - Saving draft for case type: {}",
                probateCaseDetails.getCaseData().getClass().getSimpleName());
        CaseData caseData = probateCaseDetails.getCaseData();
        CaseType caseType = CaseType.getCaseType(caseData);
        if (!caseType.equals(CaseType.GRANT_OF_REPRESENTATION)) {
            Pair<String, String> searchFieldValuePair = searchFieldFactory.getSearchFieldValuePair(caseType, caseData);
            String searchValue = searchFieldValuePair.getRight();
            Assert.isTrue(searchValue.equals(searchField), "Applicant email on path must match case data");
        }
        SecurityDto securityDto = securityUtils.getSecurityDto();
        //for Grant of Representation, we need to find the case by id to CCD
        //for other case types, we can search by applicant email or other search fields
        Optional<ProbateCaseDetails> caseInfoOptional = caseType.equals(CaseType.GRANT_OF_REPRESENTATION)
                ? coreCaseDataService.findCaseById(searchField,securityDto)
                : coreCaseDataService.findCase(searchField, caseType, securityDto);
        log.info("saveDraft - caseInfoOptional - Saving draft for id: {} {}", searchField,
                caseInfoOptional.get().getCaseData().getPayments());
        Optional<ProbateCaseDetails> tempCaseInfo = coreCaseDataService.findCase(searchField, caseType, securityDto);
        log.info("saveDraft - tempCaseInfo - Saving draft for id: {} {}", searchField,
                tempCaseInfo.get().getCaseData().getPayments());
        return saveCase(securityDto, caseType, caseData, caseInfoOptional, asCaseworker, eventDescription);

    }

    private ProbateCaseDetails saveCase(SecurityDto securityDto, CaseType caseType, CaseData caseData,
                                        Optional<ProbateCaseDetails> caseResponseOptional, Boolean asCaseworker,
                                        String eventDescription) {
        CaseEvents caseEvents = eventFactory.getCaseEvents(caseType);
        if (caseResponseOptional.isPresent()) {
            ProbateCaseDetails caseResponse = caseResponseOptional.get();
            CaseState state = caseResponse.getCaseInfo().getState();
            log.info("Found case with case Id: {} at state: {}", caseResponse.getCaseInfo().getCaseId(),
                state.getName());
            EventId eventId = eventMap.get(state).apply(caseEvents);
            if (EventId.GOP_CITIZEN_HUB_RESPONSE_DRAFT.equals(eventId) && isSubmitHubResponse(caseData)) {
                eventId = EventId.GOP_CITIZEN_HUB_RESPONSE;
            } else if (EventId.GOP_UPDATE_DRAFT.equals(eventId) && isTaskListPage(eventDescription)) {
                eventId = EventId.KEEP_DRAFT;
            }
            if (asCaseworker) {
                return coreCaseDataService
                    .updateCaseAsCaseworker(caseResponse.getCaseInfo().getCaseId(), caseData, eventId, securityDto);
            } else {
                return coreCaseDataService
                    .updateCase(caseResponse.getCaseInfo().getCaseId(), caseData, eventId,
                    securityDto, eventDescription);
            }
        }
        log.info("No case found");
        return coreCaseDataService.createCase(caseData, caseEvents.getCreateDraftEventId(), securityDto);
    }

    @Override
    public ProbateCaseDetails initiateCase(ProbateCaseDetails probateCaseDetails) {
        log.info("initiateCase - Initiating case for case type: {}",
            probateCaseDetails.getCaseData().getClass().getSimpleName());
        CaseData caseData = probateCaseDetails.getCaseData();
        CaseType caseType = CaseType.getCaseType(caseData);
        SecurityDto securityDto = securityUtils.getSecurityDto();
        CaseEvents caseEvents = eventFactory.getCaseEvents(caseType);
        final EventId eventId =
            CaseType.CAVEAT == caseType
                ? caseEvents.getCreateCaseApplicationEventId() : caseEvents.getCreateDraftEventId();
        return coreCaseDataService.createCase(caseData, eventId, securityDto);
    }

    @Override
    public ProbateCaseDetails saveCaseAsCaseworker(String searchField, ProbateCaseDetails probateCaseDetails) {
        log.info("saveCaseAsCaseworker - Saving draft as caseworkefor case type: {}",
            probateCaseDetails.getCaseData().getClass().getSimpleName());
        return saveCase(searchField, probateCaseDetails, Boolean.TRUE, "save case as case worker");
    }

    @Override
    public ProbateCaseDetails getCaseByInvitationId(String invitationId, CaseType caseType) {
        log.info("Getting case of caseType: {}", caseType.getName());
        SecurityDto securityDto = securityUtils.getSecurityDto();
        Optional<ProbateCaseDetails> caseResponseOptional = coreCaseDataService
            .findCaseByInviteId(invitationId, caseType, securityDto);
        return caseResponseOptional.orElseThrow(CaseNotFoundException::new);
    }


    @Override
    public ProbateCaseDetails validate(String searchField, CaseType caseType) {
        log.info("Validating case of caseType: {}, applicationId: {}", caseType.getName(), searchField);
        ProbateCaseDetails probateCaseDetails = getCase(searchField, caseType);
        validationService.validate(probateCaseDetails);
        return probateCaseDetails;
    }

    private boolean isSubmitHubResponse(CaseData caseData) {
        if (!(caseData instanceof GrantOfRepresentationData data)) {
            log.error("Invalid caseData type: {}", caseData.getClass().getSimpleName());
            return false;
        }

        boolean hasCitizenResponseCheckbox = data.getCitizenResponseCheckbox() != null
                && data.getCitizenResponseCheckbox();
        boolean hasDocumentUploadIssue = data.getDocumentUploadIssue() != null && data.getDocumentUploadIssue();
        boolean noResponseOrUploadedDocs = (data.getCitizenResponse() == null || data.getCitizenResponse().isEmpty())
                && (data.getCitizenDocumentsUploaded() == null || data.getCitizenDocumentsUploaded().isEmpty());
        boolean isSaveAndClose = data.getIsSaveAndClose() != null && data.getIsSaveAndClose();
        return hasCitizenResponseCheckbox
            || (hasDocumentUploadIssue && !isSaveAndClose && noResponseOrUploadedDocs);
    }

    private boolean isTaskListPage(String eventDescription) {
        return eventDescription != null && eventDescription.contains("task-list");
    }
}
