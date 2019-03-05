package uk.gov.hmcts.probate.services.submit.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.probate.services.submit.validation.validator.CaseDataValidator;
import uk.gov.hmcts.probate.services.submit.validation.validator.CaveatValidator;
import uk.gov.hmcts.probate.services.submit.validation.validator.IntestacyValidator;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantOfRepresentationData;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantType;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseDataValidatorFactory {

    private final IntestacyValidator intestacyValidator;
    private final CaveatValidator caveatValidator;

    public Optional<CaseDataValidator> getValidator(CaseData caseData) {
        Optional<CaseDataValidator> optionalCaseDataValidator = Optional.empty();
        if (CaseType.getCaseType(caseData).equals(CaseType.GRANT_OF_REPRESENTATION)) {
            GrantOfRepresentationData gop = (GrantOfRepresentationData) caseData;
            if (gop.getGrantType().equals(GrantType.INTESTACY))
                optionalCaseDataValidator = Optional.of(intestacyValidator);
        } else if (CaseType.getCaseType(caseData).equals(CaseType.CAVEAT)) {
            optionalCaseDataValidator = Optional.of(caveatValidator);
        }
        return optionalCaseDataValidator;
    }
}
