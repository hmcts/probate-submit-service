package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.probate.security.SecurityDTO;
import uk.gov.hmcts.probate.services.submit.core.SearchFieldFactory;
import uk.gov.hmcts.probate.services.submit.services.CoreCaseDataService;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CaseInfo;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.EventId;
import uk.gov.hmcts.reform.probate.model.cases.JurisdictionId;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CcdClientApi implements CoreCaseDataService {

    private static final String CASE_QUERY_PARAM = "case.";

    private final CoreCaseDataApi coreCaseDataApi;

    private final CaseDetailsToCaseDataMapper caseDetailsToCaseDataMapper;

    private final SearchFieldFactory searchFieldFactory;

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
        CaseDataContent caseDataContent = createCaseDataContent(caseData, eventId, startEventResponse);
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
    public ProbateCaseDetails createCase(CaseData caseData, EventId eventId, SecurityDTO securityDTO) {
        CaseType caseType = CaseType.getCaseType(caseData);
        log.info("Create case for caseType: {}, caseId: {}, eventId: {}",
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
        CaseDataContent caseDataContent = createCaseDataContent(caseData, eventId, startEventResponse);
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
    public Optional<ProbateCaseDetails> findCase(String searchField, CaseType caseType, SecurityDTO securityDTO) {
        log.info("Search for case in CCD for Citizen, caseType: {}", caseType.getName());
        List<CaseDetails> caseDetails = coreCaseDataApi.searchForCitizen(
                securityDTO.getAuthorisation(),
                securityDTO.getServiceAuthorisation(),
                securityDTO.getUserId(),
                JurisdictionId.PROBATE.name(),
                caseType.getName(),
                ImmutableMap.of(CASE_QUERY_PARAM + searchFieldFactory.getSearchFieldName(caseType), searchField));
        if (caseDetails == null) {
            return Optional.empty();
        }
        if (caseDetails.size() > 1) {
            throw new IllegalStateException("Multiple cases exist with applicant email provided!");
        }
        return caseDetails.stream().findFirst().map(this::createCaseResponse);
    }

    private ProbateCaseDetails createCaseResponse(CaseDetails caseDetails) {
        CaseInfo caseInfo = new CaseInfo();
        caseInfo.setCaseId(caseDetails.getId().toString());
        caseInfo.setState(caseDetails.getState());

        return ProbateCaseDetails.builder()
                .caseData(caseDetailsToCaseDataMapper.map(caseDetails))
                .caseInfo(caseInfo)
                .build();
    }

    private CaseDataContent createCaseDataContent(CaseData caseData, EventId eventId, StartEventResponse startEventResponse) {
        return CaseDataContent.builder()
                .event(createEvent(eventId))
                .eventToken(startEventResponse.getToken())
                .data(caseData)
                .build();
    }

    private Event createEvent(EventId eventId) {
        return Event.builder()
                .id(eventId.getName())
                .description("Probate application")
                .summary("Probate application")
                .build();
    }
}
