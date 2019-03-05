package uk.gov.hmcts.probate.services.submit.core.proccessors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import uk.gov.hmcts.probate.security.SecurityDTO;
import uk.gov.hmcts.probate.security.SecurityUtils;
import uk.gov.hmcts.probate.services.submit.core.SearchFieldFactory;
import uk.gov.hmcts.probate.services.submit.model.v2.exception.CaseNotFoundException;
import uk.gov.hmcts.probate.services.submit.services.CoreCaseDataService;
import uk.gov.hmcts.probate.services.submit.validation.CaseDataValidatorFactory;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.SubmitResult;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public abstract class AbstractSubmissionsProcessor {

    private final SecurityUtils securityUtils;
    private final SearchFieldFactory searchFieldFactory;
    private final CaseDataValidatorFactory caseDataValidatorFactory;
    private final CoreCaseDataService coreCaseDataService;

    public SubmitResult process(String identifier, ProbateCaseDetails caseRequest) {
        log.info("Processing case type: {}", caseRequest.getCaseData().getClass().getSimpleName());
        CaseData caseData = caseRequest.getCaseData();
        CaseType caseType = CaseType.getCaseType(caseData);
        assertIndentifierMatchesCase(identifier, caseData, caseType);
        SubmitResult submitResult = new SubmitResult();
        validateCase(caseData, submitResult);
        if (noValidationCaseErrorsFound(submitResult)) {
            submitResult.setProbateCaseDetails(processCase(identifier, caseData, caseType, securityUtils.getSecurityDTO()));
        } else {
            submitResult.setProbateCaseDetails(caseRequest);
        }
        return submitResult;
    }

    private Boolean noValidationCaseErrorsFound(SubmitResult submitResult) {
        if (submitResult.getValidatorResults().isPresent()) {
            return submitResult.isValid();
        }
        return true;
    }

    protected abstract ProbateCaseDetails processCase(String identifier, CaseData caseData, CaseType caseType, SecurityDTO securityDTO);


    protected ProbateCaseDetails findCase(String searchField, CaseType caseType, SecurityDTO securityDTO) {
        Optional<ProbateCaseDetails> caseResponseOptional = coreCaseDataService.
                findCase(searchField, caseType, securityDTO);
        return caseResponseOptional.orElseThrow(CaseNotFoundException::new);
    }

    private void assertIndentifierMatchesCase(String identifier, CaseData caseData, CaseType caseType) {
        String searchFieldValueInBody = searchFieldFactory.getSearchFieldValuePair(caseType, caseData).getRight();
        Assert.isTrue(searchFieldValueInBody.equals(identifier), "Applicant email on path must match case data");
    }

    private void validateCase(CaseData caseData, SubmitResult submitResult) {
        caseDataValidatorFactory.getValidator(caseData).ifPresent(caseDataValidator -> {
            submitResult.setValidatorResults(caseDataValidator.validate(caseData));
        });
    }


}
