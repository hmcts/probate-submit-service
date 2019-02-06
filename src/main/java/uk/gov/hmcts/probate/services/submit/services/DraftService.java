package uk.gov.hmcts.probate.services.submit.services;

import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;

public interface DraftService {

    ProbateCaseDetails saveDraft(String searchField, ProbateCaseDetails caseRequest);

}
