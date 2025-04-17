package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.probate.security.SecurityDto;
import uk.gov.hmcts.probate.services.submit.core.SearchFieldFactory;
import uk.gov.hmcts.probate.services.submit.services.CoreCaseDataService;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.EventId;
import uk.gov.hmcts.reform.probate.model.cases.JurisdictionId;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CcdClientApi implements CoreCaseDataService {

    private static final String EVENT_SUMMARY = "Probate application";

    private final CoreCaseDataApi coreCaseDataApi;

    private final CaseAccessApi caseAccessApi;

    private final CaseContentBuilder caseContentBuilder;

    private final CaseResponseBuilder caseResponseBuilder;

    private final SearchFieldFactory searchFieldFactory;

    private final CcdElasticSearchQueryBuilder elasticSearchQueryBuilder;

    @Override
    public ProbateCaseDetails updateCase(String caseId, LocalDateTime lastModifiedDateTime, CaseData caseData,
                                         EventId eventId, SecurityDto securityDto, String eventDescription) {
        CaseType caseType = CaseType.getCaseType(caseData);
        log.info("Update case for caseType: {}, caseId: {}, eventId: {}",
            caseType.getName(), caseId, eventId.getName());
        log.info("Retrieve event token from CCD for Citizen, caseType: {}, caseId: {}, eventId: {}",
            caseType.getName(), caseId, eventId.getName());
        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCitizen(
            securityDto.getAuthorisation(),
            securityDto.getServiceAuthorisation(),
            securityDto.getUserId(),
            JurisdictionId.PROBATE.name(),
            caseType.getName(),
            caseId,
            eventId.getName()
        );
        CaseDataContent caseDataContent =
            createCaseDataContent(caseData, eventId, startEventResponse, EVENT_SUMMARY, eventDescription);
        log.info("Submit event to CCD for Citizen, caseType: {}, caseId: {}",
            caseType.getName(), caseId);
        CaseDetails caseDetails = coreCaseDataApi.submitEventForCitizen(
            securityDto.getAuthorisation(),
            securityDto.getServiceAuthorisation(),
            securityDto.getUserId(),
            JurisdictionId.PROBATE.name(),
            caseType.getName(),
            caseId,
            false,
            caseDataContent
        );
        return createCaseResponse(caseDetails);
    }

    @Override
    public ProbateCaseDetails updateCaseAsCaseworker(String caseId, LocalDateTime lastModifiedDateTime,
                                                     CaseData caseData, EventId eventId, SecurityDto securityDto) {
        return updateCaseAsCaseworker(caseId, lastModifiedDateTime, caseData, eventId, securityDto, EVENT_SUMMARY);
    }

    @Override
    public ProbateCaseDetails updateCaseAsCaseworker(String caseId, LocalDateTime lastModifiedDateTime,
                                                     CaseData caseData, EventId eventId,
                                                     SecurityDto securityDto, String eventDescriptor) {
        CaseType caseType = CaseType.getCaseType(caseData);
        log.info("Update case as for caseType: {}, caseId: {}, eventId: {}",
            caseType.getName(), caseId, eventId.getName());
        log.info("Retrieve event token from CCD for Caseworker, caseType: {}, caseId: {}, eventId: {}",
            caseType.getName(), caseId, eventId.getName());
        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
            securityDto.getAuthorisation(),
            securityDto.getServiceAuthorisation(),
            securityDto.getUserId(),
            JurisdictionId.PROBATE.name(),
            caseType.getName(),
            caseId,
            eventId.getName()
        );
        CaseDataContent caseDataContent = createCaseDataContent(caseData, eventId, startEventResponse,
            eventDescriptor,  eventDescriptor);
        log.info("Submit event to CCD for Caseworker, caseType: {}, caseId: {}",
            caseType.getName(), caseId);
        CaseDetails caseDetails = coreCaseDataApi.submitEventForCaseWorker(
            securityDto.getAuthorisation(),
            securityDto.getServiceAuthorisation(),
            securityDto.getUserId(),
            JurisdictionId.PROBATE.name(),
            caseType.getName(),
            caseId,
            false,
            caseDataContent
        );
        return createCaseResponse(caseDetails);
    }

    @Override
    public ProbateCaseDetails createCase(CaseData caseData, EventId eventId, SecurityDto securityDto) {
        CaseType caseType = CaseType.getCaseType(caseData);
        log.info("Create case for caseType: {}, eventId: {}",
            caseType.getName(), eventId.getName());
        log.info("Retrieve event token from CCD for Citizen, caseType: {}, eventId: {}",
            caseType.getName(), eventId.getName());
        StartEventResponse startEventResponse = coreCaseDataApi.startForCitizen(
            securityDto.getAuthorisation(),
            securityDto.getServiceAuthorisation(),
            securityDto.getUserId(),
            JurisdictionId.PROBATE.name(),
            caseType.getName(),
            eventId.getName()
        );
        CaseDataContent caseDataContent =
            createCaseDataContent(caseData, eventId, startEventResponse, EVENT_SUMMARY, "create case");
        log.info("Submit event to CCD for Citizen, caseType: {}", caseType.getName());
        CaseDetails caseDetails = coreCaseDataApi.submitForCitizen(
            securityDto.getAuthorisation(),
            securityDto.getServiceAuthorisation(),
            securityDto.getUserId(),
            JurisdictionId.PROBATE.name(),
            caseType.getName(),
            false,
            caseDataContent
        );
        return createCaseResponse(caseDetails);
    }

    @Override
    public Optional<ProbateCaseDetails> findCaseByInviteId(String inviteId, CaseType caseType,
                                                           SecurityDto securityDto) {
        log.info("Search for case in CCD for Citizen, caseType: {}", caseType.getName());

        String searchString =
            elasticSearchQueryBuilder.buildQuery(inviteId, searchFieldFactory.getSearchInviteFieldName());
        List<CaseDetails> caseDetails = coreCaseDataApi.searchCases(
            securityDto.getAuthorisation(),
            securityDto.getServiceAuthorisation(),
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
    public Optional<ProbateCaseDetails> findCaseByApplicantEmail(String searchField, CaseType caseType,
                                                                 SecurityDto securityDto) {
        log.info("Search for case in CCD for Citizen, caseType: {}", caseType.getName());
        String searchString =
            elasticSearchQueryBuilder.buildQuery(searchField, searchFieldFactory.getSearchApplicantEmailFieldName());
        List<CaseDetails> caseDetails = coreCaseDataApi.searchCases(
            securityDto.getAuthorisation(),
            securityDto.getServiceAuthorisation(),
            caseType.getName(),
            searchString).getCases();
        if (caseDetails == null) {
            return Optional.empty();
        }
        return caseDetails.stream().findFirst().map(this::createCaseResponse);
    }

    @Override
    public Optional<ProbateCaseDetails> findCase(String searchValue, CaseType caseType, SecurityDto securityDto) {
        log.info("Search for case in ES for Citizen by searchCases caseType:{}, searchValue:{}",
                caseType.getName(), searchValue);
        String searchString =
            elasticSearchQueryBuilder.buildQuery(searchValue, searchFieldFactory.getEsSearchFieldName(caseType));
        List<CaseDetails> caseDetails = coreCaseDataApi.searchCases(
            securityDto.getAuthorisation(),
            securityDto.getServiceAuthorisation(),
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
    public List<ProbateCaseDetails> findCases(CaseType caseType, SecurityDto securityDto) {
        log.info("Search for case in CCD for Citizen by searchForCitizen, caseType: {}", caseType.getName());
        List<CaseDetails> caseDetails = coreCaseDataApi.searchForCitizen(
                securityDto.getAuthorisation(),
                securityDto.getServiceAuthorisation(),
                securityDto.getUserId(),
                JurisdictionId.PROBATE.name(),
                caseType.getName(),
                new java.util.HashMap<String, String>());
        return caseDetails.stream().map(this::createCaseResponse).collect(Collectors.toList());
    }

    @Override
    public Optional<ProbateCaseDetails> findCaseById(String caseId, SecurityDto securityDto) {
        CaseDetails caseDetails = coreCaseDataApi.getCase(
            securityDto.getAuthorisation(),
            securityDto.getServiceAuthorisation(),
            caseId);
        if (caseDetails == null) {
            return Optional.empty();
        }
        return Optional.of(createCaseResponse(caseDetails));
    }

    private ProbateCaseDetails createCaseResponse(CaseDetails caseDetails) {
        return caseResponseBuilder.createCaseResponse(caseDetails);
    }

    private CaseDataContent createCaseDataContent(CaseData caseData, EventId eventId,
                                                  StartEventResponse startEventResponse,
                                                  String eventSummary, String eventDescription) {
        return caseContentBuilder.createCaseDataContent(caseData, eventId, startEventResponse,
            eventSummary, eventDescription);
    }

}
