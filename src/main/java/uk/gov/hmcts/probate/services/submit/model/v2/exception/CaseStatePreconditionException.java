package uk.gov.hmcts.probate.services.submit.model.v2.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.hmcts.reform.probate.model.PaymentStatus;
import uk.gov.hmcts.reform.probate.model.cases.CaseState;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class CaseStatePreconditionException extends RuntimeException {

    public CaseStatePreconditionException(CaseState caseState, PaymentStatus paymentStatus) {
        super("Event ID not present for case state: " + caseState + " and payment status: "
                + paymentStatus + " combination");
    }
}
