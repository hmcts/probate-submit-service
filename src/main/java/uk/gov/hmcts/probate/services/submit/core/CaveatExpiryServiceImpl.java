package uk.gov.hmcts.probate.services.submit.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.probate.security.SecurityDTO;
import uk.gov.hmcts.probate.security.SecurityUtils;
import uk.gov.hmcts.probate.services.submit.clients.v2.ccd.CaseResponseBuilder;
import uk.gov.hmcts.probate.services.submit.clients.v2.ccd.CcdElasticSearchQueryBuilder;
import uk.gov.hmcts.probate.services.submit.services.CaveatExpiryService;
import uk.gov.hmcts.probate.services.submit.services.CoreCaseDataService;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CaseState;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.EventId;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.caveat.CaveatData;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.probate.model.cases.EventId.CAVEAT_APPLY_FOR_AWAITING_WARNING_RESPONSE;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.CAVEAT_APPLY_FOR_WARNNG_VALIDATION;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.CAVEAT_EXPIRED_FOR_AWAITING_RESOLUTION;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.CAVEAT_EXPIRED_FOR_CAVEAT_NOT_MATCHED;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaveatExpiryServiceImpl implements CaveatExpiryService {

    private final CoreCaseDataService coreCaseDataService;
    
    private final CaseResponseBuilder caseResponseBuilder;

    private final SecurityUtils securityUtils;

    private final CoreCaseDataApi coreCaseDataApi;

    private final CcdElasticSearchQueryBuilder elasticSearchQueryBuilder;

    private static final String EVENT_DESCRIPTOR_CAVEAT_EXPIRED = "Caveat Auto Expired";

    @Override
    public List<ProbateCaseDetails> expireCaveats(String expiryDate) {
        SecurityDTO securityDTO = securityUtils.getSecurityDTO();
        log.info("Search for expired Caveats for expiryDate: {}", expiryDate);
        String searchString = elasticSearchQueryBuilder.buildQueryForCaveatExpiry(expiryDate);
        List<CaseDetails> caseDetails = coreCaseDataApi.searchCases(
            securityDTO.getAuthorisation(),
            securityDTO.getServiceAuthorisation(),
            CaseType.CAVEAT.getName(),
            searchString).getCases();

        log.info("Caveats found for expiry: {}", caseDetails.size());

        List<ProbateCaseDetails> expiredCaveats = caseDetails.stream().map(this::createCaseResponse).collect(Collectors.toList());

        for (ProbateCaseDetails probateCaseDetails : expiredCaveats) {
            EventId eventIdToStart = getEventIdForCaveatToExpireGivenPreconditionState(probateCaseDetails.getCaseInfo().getState());
            updateAutoExpiredCaveat(((CaveatData)probateCaseDetails.getCaseData()));
            updateCaseAsCaseworker(probateCaseDetails.getCaseInfo().getCaseId(), probateCaseDetails.getCaseData(), eventIdToStart,
                securityDTO, EVENT_DESCRIPTOR_CAVEAT_EXPIRED);
            log.info("Caveat autoExpired: {}", probateCaseDetails.getCaseInfo().getCaseId());
        }

        return expiredCaveats;
    }

    private void updateCaseAsCaseworker(String caseId, CaseData caseData, EventId eventIdToStart, SecurityDTO securityDTO, 
                                        String eventDescriptorCaveatExpired) {
        try {
            coreCaseDataService.updateCaseAsCaseworker(caseId, caseData, eventIdToStart, securityDTO, eventDescriptorCaveatExpired);
        } catch (RuntimeException e) {
            log.info("Caveat autoExpire failure for case: {}, due to {}", caseId, e.getMessage());
        }
    }

    private ProbateCaseDetails createCaseResponse(CaseDetails caseDetails) {
        return caseResponseBuilder.createCaseResponse(caseDetails);    
    }

    private void updateAutoExpiredCaveat(CaveatData caveatData) {
        caveatData.setAutoClosedExpiry(Boolean.TRUE);
    }

    private EventId getEventIdForCaveatToExpireGivenPreconditionState(CaseState caveatState) {
        EventId eventId = null;
        switch (caveatState) {
            case CAVEAT_NOT_MATCHED:
                eventId = CAVEAT_EXPIRED_FOR_CAVEAT_NOT_MATCHED;
                break;
            case CAVEAT_AWAITING_RESOLUTION:
                eventId = CAVEAT_EXPIRED_FOR_AWAITING_RESOLUTION;
                break;
            case CAVEAT_AWAITING_WARNING_RESPONSE:
                eventId = CAVEAT_APPLY_FOR_AWAITING_WARNING_RESPONSE;
                break;
            case CAVEAT_WARNING_VALIDATION:
                eventId = CAVEAT_APPLY_FOR_WARNNG_VALIDATION;
                break;
            default:
                throw new IllegalStateException("Unexpected state for Caveat Auto Expiry: " + caveatState);
        }
        return eventId;
    }

}
