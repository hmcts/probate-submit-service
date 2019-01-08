package uk.gov.hmcts.probate.services.submit.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import uk.gov.hmcts.probate.security.SecurityDTO;
import uk.gov.hmcts.probate.security.SecurityUtils;
import uk.gov.hmcts.probate.services.submit.clients.v2.ccd.CaseState;
import uk.gov.hmcts.probate.services.submit.clients.v2.ccd.EventId;
import uk.gov.hmcts.probate.services.submit.model.v2.exception.CaseNotFoundException;
import uk.gov.hmcts.probate.services.submit.model.v2.exception.CaseStatePreconditionException;
import uk.gov.hmcts.probate.services.submit.services.CoreCaseDataService;
import uk.gov.hmcts.probate.services.submit.services.SubmissionsService;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;

import java.util.Optional;

@Slf4j


@Component
@RequiredArgsConstructor
public class SubmissionsServiceImpl implements SubmissionsService {

    private final CoreCaseDataService coreCaseDataService;

    private final SecurityUtils securityUtils;

    @Override
    public ProbateCaseDetails submit(String applicantEmail, ProbateCaseDetails caseRequest) {
        log.info("Submitting for case type: {}", caseRequest.getCaseData().getClass().getSimpleName());
        CaseData caseData = caseRequest.getCaseData();
        Assert.isTrue(caseData.getPrimaryApplicantEmailAddress().equals(applicantEmail),
                "Applicant email on path must match case data");
        SecurityDTO securityDTO = securityUtils.getSecurityDTO();
        ProbateCaseDetails caseResponse = findCase(applicantEmail, CaseType.getCaseType(caseData), securityDTO);
        log.info("Found case with case Id: {}", caseResponse.getCaseInfo().getCaseId());
        CaseState state = CaseState.getState(caseResponse.getCaseInfo().getState());
        checkStatePrecondition(state);
        String caseId = caseResponse.getCaseInfo().getCaseId();
        return coreCaseDataService.updateCase(caseId, caseData, EventId.CREATE_APPLICATION, securityDTO);
    }

    private ProbateCaseDetails findCase(String applicantEmail, CaseType caseType, SecurityDTO securityDTO) {
        Optional<ProbateCaseDetails> caseResponseOptional = coreCaseDataService.
                findCase(applicantEmail, caseType, securityDTO);
        return caseResponseOptional.orElseThrow(() -> new CaseNotFoundException());
    }

    private void checkStatePrecondition(CaseState caseState) {
        if (!caseState.equals(CaseState.DRAFT)) {
            throw new CaseStatePreconditionException(caseState, EventId.CREATE_APPLICATION);
        }
    }
}
