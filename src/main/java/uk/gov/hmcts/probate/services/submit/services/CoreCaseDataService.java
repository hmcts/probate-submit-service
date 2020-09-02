package uk.gov.hmcts.probate.services.submit.services;

import uk.gov.hmcts.probate.security.SecurityDTO;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.EventId;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;

import java.util.List;
import java.util.Optional;

public interface CoreCaseDataService {

    Optional<ProbateCaseDetails> findCaseByInviteId(String inviteId, CaseType caseType, SecurityDTO securityDTO);

    Optional<ProbateCaseDetails> findCase(String searchField, CaseType caseType, SecurityDTO securityDTO);

    List<ProbateCaseDetails> findCases(CaseType caseType, SecurityDTO securityDTO);

    Optional<ProbateCaseDetails> findCaseById(String caseId, SecurityDTO securityDTO);

    ProbateCaseDetails updateCase(String caseId, CaseData caseData, EventId eventId, SecurityDTO securityDTO);

    ProbateCaseDetails createCase(CaseData caseData, EventId eventId, SecurityDTO securityDTO);

    ProbateCaseDetails updateCaseAsCaseworker(String caseId, CaseData caseData, EventId eventId,
                                              SecurityDTO securityDTO);

    ProbateCaseDetails updateCaseAsCaseworker(String caseId, CaseData caseData, EventId eventId,
                                                     SecurityDTO securityDTO, String eventDescriptor);
    
    Optional<ProbateCaseDetails> findCaseByApplicantEmail(String searchField, CaseType caseType, SecurityDTO securityDTO);

    void grantAccessForCase(CaseType caseType, String caseId, String userId, SecurityDTO securityDTO);
}
