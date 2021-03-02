package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.probate.security.SecurityDTO;
import uk.gov.hmcts.probate.services.submit.core.SearchFieldFactory;
import uk.gov.hmcts.probate.services.submit.services.CoreCaseDataService;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.client.model.UserId;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.EventId;
import uk.gov.hmcts.reform.probate.model.cases.JurisdictionId;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CcdClientApi implements CoreCaseDataService {

    private static final String EVENT_DESCRIPTOR = "Probate application";

    private final CoreCaseDataApi coreCaseDataApi;

    private final CaseAccessApi caseAccessApi;

    private final CaseContentBuilder caseContentBuilder;

    private final CaseResponseBuilder caseResponseBuilder;

    private final SearchFieldFactory searchFieldFactory;

    private final CcdElasticSearchQueryBuilder elasticSearchQueryBuilder;

    @Override
    public ProbateCaseDetails updateCase(String caseId, CaseData caseData, EventId eventId,
                                         SecurityDTO securityDTO) {
        CaseType caseType = CaseType.getCaseType(caseData);
        log.info("Update case for caseType: {}, caseId: {}, eventId: {}",
                caseType.getName(), caseId, eventId.getName());
        log.info("Retrieve event token from CCD for Citizen, caseType: {}, caseId: {}, eventId: {}",
                caseType.getName(), caseId, eventId.getName());
        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCitizen(
                securityDTO.getAuthorisation(),
                securityDTO.getServiceAuthorisation(),
                securityDTO.getUserId(),
                JurisdictionId.PROBATE.name(),
                caseType.getName(),
                caseId,
                eventId.getName()
        );
        CaseDataContent caseDataContent = createCaseDataContent(caseData, eventId, startEventResponse, EVENT_DESCRIPTOR);
        log.info("Submit event to CCD for Citizen, caseType: {}, caseId: {}",
                caseType.getName(), caseId);
        CaseDetails caseDetails = coreCaseDataApi.submitEventForCitizen(
                securityDTO.getAuthorisation(),
                securityDTO.getServiceAuthorisation(),
                securityDTO.getUserId(),
                JurisdictionId.PROBATE.name(),
                caseType.getName(),
                caseId,
                false,
                caseDataContent
        );
        return createCaseResponse(caseDetails);
    }

    @Override
    public ProbateCaseDetails updateCaseAsCaseworker(String caseId, CaseData caseData, EventId eventId,
                                                     SecurityDTO securityDTO) {
        return updateCaseAsCaseworker(caseId, caseData, eventId, securityDTO, EVENT_DESCRIPTOR);
    }

    @Override
    public ProbateCaseDetails updateCaseAsCaseworker(String caseId, CaseData caseData, EventId eventId,
                                                     SecurityDTO securityDTO, String eventDescriptor) {
        CaseType caseType = CaseType.getCaseType(caseData);
        log.info("Update case as for caseType: {}, caseId: {}, eventId: {}",
            caseType.getName(), caseId, eventId.getName());
        log.info("Retrieve event token from CCD for Caseworker, caseType: {}, caseId: {}, eventId: {}",
            caseType.getName(), caseId, eventId.getName());
        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
            securityDTO.getAuthorisation(),
            securityDTO.getServiceAuthorisation(),
            securityDTO.getUserId(),
            JurisdictionId.PROBATE.name(),
            caseType.getName(),
            caseId,
            eventId.getName()
        );
        CaseDataContent caseDataContent = createCaseDataContent(caseData, eventId, startEventResponse, eventDescriptor);
        log.info("Submit event to CCD for Caseworker, caseType: {}, caseId: {}",
            caseType.getName(), caseId);
        CaseDetails caseDetails = coreCaseDataApi.submitEventForCaseWorker(
            securityDTO.getAuthorisation(),
            securityDTO.getServiceAuthorisation(),
            securityDTO.getUserId(),
            JurisdictionId.PROBATE.name(),
            caseType.getName(),
            caseId,
            false,
            caseDataContent
        );
        return createCaseResponse(caseDetails);
    }

    @Override
    public ProbateCaseDetails createCase(CaseData caseData, EventId eventId, SecurityDTO securityDTO) {
        CaseType caseType = CaseType.getCaseType(caseData);
        log.info("Create case for caseType: {}, caseType: {}, eventId: {}",
                caseType.getName(), eventId.getName());
        log.info("Retrieve event token from CCD for Citizen, caseType: {}, eventId: {}",
                caseType.getName(), eventId.getName());
        StartEventResponse startEventResponse = coreCaseDataApi.startForCitizen(
                securityDTO.getAuthorisation(),
                securityDTO.getServiceAuthorisation(),
                securityDTO.getUserId(),
                JurisdictionId.PROBATE.name(),
                caseType.getName(),
                eventId.getName()
        );
        CaseDataContent caseDataContent = createCaseDataContent(caseData, eventId, startEventResponse, EVENT_DESCRIPTOR);
        log.info("Submit event to CCD for Citizen, caseType: {}", caseType.getName());
        CaseDetails caseDetails = coreCaseDataApi.submitForCitizen(
                securityDTO.getAuthorisation(),
                securityDTO.getServiceAuthorisation(),
                securityDTO.getUserId(),
                JurisdictionId.PROBATE.name(),
                caseType.getName(),
                false,
                caseDataContent
        );
        return createCaseResponse(caseDetails);
    }

    @Override
    public Optional<ProbateCaseDetails> findCaseByInviteId(String inviteId, CaseType caseType, SecurityDTO securityDTO) {
        log.info("Search for case in CCD for Citizen, caseType: {}", caseType.getName());

        String searchString = elasticSearchQueryBuilder.buildQuery(inviteId, searchFieldFactory.getSearchInviteFieldName());
        List<CaseDetails> caseDetails = coreCaseDataApi.searchCases(
                securityDTO.getAuthorisation(),
                securityDTO.getServiceAuthorisation(),
                caseType.getName(),
                searchString).getCases();
        if (caseDetails == null) {
            return Optional.empty();
        }
        if (caseDetails.size() > 1) {
            throw new IllegalStateException("Multiple cases exist with invite id provided!");
        }
        return caseDetails.stream().findFirst().map(this::createCaseResponse);
    }


    @Override
    public Optional<ProbateCaseDetails> findCaseByApplicantEmail(String searchField, CaseType caseType, SecurityDTO securityDTO) {
        log.info("Search for case in CCD for Citizen, caseType: {}", caseType.getName());
        String searchString = elasticSearchQueryBuilder.buildQuery(searchField, searchFieldFactory.getSearchApplicantEmailFieldName());
        List<CaseDetails> caseDetails = coreCaseDataApi.searchCases(
                securityDTO.getAuthorisation(),
                securityDTO.getServiceAuthorisation(),
                caseType.getName(),
                searchString).getCases();
        if (caseDetails == null) {
            return Optional.empty();
        }
        return caseDetails.stream().findFirst().map(this::createCaseResponse);
    }

    @Override
    public Optional<ProbateCaseDetails> findCase(String searchValue, CaseType caseType, SecurityDTO securityDTO) {
        log.info("Search for case in CCD for Citizen, caseType: {}", caseType.getName());
        String searchString = elasticSearchQueryBuilder.buildQuery(searchValue, searchFieldFactory.getEsSearchFieldName(caseType));
        List<CaseDetails> caseDetails = coreCaseDataApi.searchCases(
                securityDTO.getAuthorisation(),
                securityDTO.getServiceAuthorisation(),
                caseType.getName(),
                searchString).getCases();
        if (caseDetails == null) {
            return Optional.empty();
        }
        if (caseDetails.size() > 1) {
            throw new IllegalStateException("Multiple cases exist with applicant email provided!");
        }
        return caseDetails.stream().findFirst().map(this::createCaseResponse);
    }

    @Override
    public List<ProbateCaseDetails> findCases(CaseType caseType, SecurityDTO securityDTO) {
        log.info("Search for case in CCD for Citizen, caseType: {}", caseType.getName());
        String searchString = elasticSearchQueryBuilder.buildFindAllCasesQuery();
        List<CaseDetails> caseDetails = coreCaseDataApi.searchCases(
                securityDTO.getAuthorisation(),
                securityDTO.getServiceAuthorisation(),
                caseType.getName(),
                searchString).getCases();
        return caseDetails.stream().map(this::createCaseResponse).collect(Collectors.toList());
    }

    @Override
    public Optional<ProbateCaseDetails> findCaseById(String caseId, SecurityDTO securityDTO) {
        CaseDetails caseDetails = coreCaseDataApi.getCase(
                securityDTO.getAuthorisation(),
                securityDTO.getServiceAuthorisation(),
                caseId);
        if (caseDetails == null) {
            return Optional.empty();
        }
        return Optional.of(createCaseResponse(caseDetails));
    }

    private ProbateCaseDetails createCaseResponse(CaseDetails caseDetails) {
        return caseResponseBuilder.createCaseResponse(caseDetails);
    }

    private CaseDataContent createCaseDataContent(CaseData caseData, EventId eventId, StartEventResponse startEventResponse, String eventDescriptor) {
        return caseContentBuilder.createCaseDataContent(caseData, eventId, startEventResponse, eventDescriptor);
    }

}
