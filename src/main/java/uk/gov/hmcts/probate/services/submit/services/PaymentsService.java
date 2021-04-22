package uk.gov.hmcts.probate.services.submit.services;

import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;

public interface PaymentsService {

    ProbateCaseDetails createCase(String searchField, ProbateCaseDetails probateCaseDetails);

    ProbateCaseDetails updateCaseByCaseId(String caseId, ProbateCaseDetails probateUpdateRequest);
}
