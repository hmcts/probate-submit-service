package uk.gov.hmcts.probate.services.submit.services;

import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.ProbatePaymentDetails;

public interface PaymentsService {

    ProbateCaseDetails addPaymentToCase(String searchField, ProbatePaymentDetails probatePaymentDetails);
}
