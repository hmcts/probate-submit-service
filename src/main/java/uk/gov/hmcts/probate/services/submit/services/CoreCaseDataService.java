package uk.gov.hmcts.probate.services.submit.services;

import uk.gov.hmcts.probate.security.SecurityDTO;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.EventId;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;

import java.util.Optional;

public interface CoreCaseDataService {

    Optional<ProbateCaseDetails> findCase(String searchField, CaseType caseType, SecurityDTO securityDTO) ;

    ProbateCaseDetails updateCase(String caseId, CaseData caseData, EventId eventId, SecurityDTO securityDTO);

    ProbateCaseDetails createCase(CaseData caseData, EventId eventId, SecurityDTO securityDTO);

}
