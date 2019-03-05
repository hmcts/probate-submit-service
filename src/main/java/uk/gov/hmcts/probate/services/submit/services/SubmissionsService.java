package uk.gov.hmcts.probate.services.submit.services;

import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.SubmitResult;

public interface SubmissionsService {

    SubmitResult updateDraftToCase(String searchField, ProbateCaseDetails probateCaseDetails);

    SubmitResult createCase(String searchField, ProbateCaseDetails probateCaseDetails);
}
