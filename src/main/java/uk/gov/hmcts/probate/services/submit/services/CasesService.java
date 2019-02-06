package uk.gov.hmcts.probate.services.submit.services;

import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;

public interface CasesService {

    ProbateCaseDetails getCase(String searchField, CaseType caseType);
}
