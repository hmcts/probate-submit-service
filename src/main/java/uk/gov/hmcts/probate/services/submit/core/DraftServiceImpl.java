package uk.gov.hmcts.probate.services.submit.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import uk.gov.hmcts.probate.security.SecurityDTO;
import uk.gov.hmcts.probate.security.SecurityUtils;
import uk.gov.hmcts.probate.services.submit.services.CoreCaseDataService;
import uk.gov.hmcts.probate.services.submit.services.DraftService;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CaseEvents;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DraftServiceImpl implements DraftService {

    private final CoreCaseDataService coreCaseDataService;

    private final SecurityUtils securityUtils;

    private final EventFactory eventFactory;

    private final SearchFieldFactory searchFieldFactory;

    @Override
    public ProbateCaseDetails saveDraft(String searchField, ProbateCaseDetails caseRequest) {
        log.info("saveDraft - Saving draft for case type: {}", caseRequest.getCaseData().getClass().getSimpleName());
        CaseData caseData = caseRequest.getCaseData();
        CaseType caseType = CaseType.getCaseType(caseData);
        Pair<String, String> searchFieldValuePair = searchFieldFactory.getSearchFieldValuePair(caseType, caseData);
        String searchValue = searchFieldValuePair.getRight();
        Assert.isTrue(searchValue.equals(searchField), "Applicant email on path must match case data");
        SecurityDTO securityDTO = securityUtils.getSecurityDTO();
        Optional<ProbateCaseDetails> caseInfoOptional = coreCaseDataService.findCase(searchField, caseType, securityDTO);
        return saveDraft(securityDTO, caseType, caseData, caseInfoOptional);
    }

    private ProbateCaseDetails saveDraft(SecurityDTO securityDTO, CaseType caseType, CaseData caseData,
                                         Optional<ProbateCaseDetails> caseResponseOptional) {
        CaseEvents caseEvents = eventFactory.getCaseEvents(caseType);
        if (caseResponseOptional.isPresent()) {
            ProbateCaseDetails caseResponse = caseResponseOptional.get();
            log.info("Found case with case Id: {}", caseResponse.getCaseInfo().getCaseId());
            return coreCaseDataService.updateCase(caseResponse.getCaseInfo().getCaseId(), caseData,
                    caseEvents.getUpdateDraftEventId(), securityDTO);
        }
        log.info("No case found");
        return coreCaseDataService.createCase(caseData, caseEvents.getCreateDraftEventId(), securityDTO);
    }
}
