package uk.gov.hmcts.probate.services.submit.services;

import uk.gov.hmcts.probate.security.SecurityDto;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.EventId;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;

import java.util.List;
import java.util.Optional;

public interface CoreCaseDataService {

    Optional<ProbateCaseDetails> findCaseByInviteId(String inviteId, CaseType caseType, SecurityDto securityDto);

    Optional<ProbateCaseDetails> findCase(String searchField, CaseType caseType, SecurityDto securityDto);

    List<ProbateCaseDetails> findCases(CaseType caseType, SecurityDto securityDto);

    Optional<ProbateCaseDetails> findCaseById(String caseId, SecurityDto securityDto);

    ProbateCaseDetails updateCase(String caseId, CaseData caseData, EventId eventId,
                                    SecurityDto securityDto, String eventDescription);

    ProbateCaseDetails createCase(CaseData caseData, EventId eventId, SecurityDto securityDto);

    ProbateCaseDetails updateCaseAsCaseworker(String caseId, CaseData caseData, EventId eventId,
                                              SecurityDto securityDto);

    ProbateCaseDetails updateCaseAsCaseworker(String caseId, CaseData caseData, EventId eventId,
                                              SecurityDto securityDto, String eventDescriptor);

    Optional<ProbateCaseDetails> findCaseByApplicantEmail(String searchField, CaseType caseType,
                                                          SecurityDto securityDto);
}
