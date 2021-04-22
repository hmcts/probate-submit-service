package uk.gov.hmcts.probate.services.submit.core;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.probate.security.SecurityDto;
import uk.gov.hmcts.probate.security.SecurityUtils;
import uk.gov.hmcts.probate.services.submit.model.v2.exception.CaseAlreadyExistsException;
import uk.gov.hmcts.probate.services.submit.services.CoreCaseDataService;
import uk.gov.hmcts.probate.services.submit.services.ValidationService;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CaseEvents;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.SubmitResult;
import uk.gov.hmcts.reform.probate.model.client.AssertFieldException;
import uk.gov.hmcts.reform.probate.model.client.ValidationError;
import uk.gov.hmcts.reform.probate.model.client.ValidationErrorResponse;

import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
@Component
public class CreateCaseSubmissionsProcessor {

    private final CoreCaseDataService coreCaseDataService;
    private final EventFactory eventFactory;
    private final RegistryService registryService;
    private final SecurityUtils securityUtils;
    private final SearchFieldFactory searchFieldFactory;
    private final ValidationService validationService;

    public SubmitResult process(String identifier, Supplier<ProbateCaseDetails> caseRequestSupplier) {
        ProbateCaseDetails caseRequest = caseRequestSupplier.get();
        log.info("Processing case type: {}", caseRequest.getCaseData().getClass().getSimpleName());
        CaseData caseData = caseRequest.getCaseData();
        CaseType caseType = CaseType.getCaseType(caseData);
        assertIdentifierMatchesCase(identifier, caseData, caseType);
        validationService.validate(caseRequest);
        return SubmitResult.builder()
            .probateCaseDetails(processCase(identifier, caseData))
            .build();
    }

    private ProbateCaseDetails processCase(String identifier, CaseData caseData) {
        SecurityDto securityDto = securityUtils.getSecurityDto();
        CaseType caseType = CaseType.getCaseType(caseData);
        checkDoesCaseExist(identifier, CaseType.getCaseType(caseData), securityDto);
        log.info("Case not found with case Id: {}", identifier);
        CaseEvents caseEvents = eventFactory.getCaseEvents(caseType);
        registryService.updateRegistry(caseData);
        return coreCaseDataService.createCase(caseData, caseEvents.getCreateCaseApplicationEventId(), securityDto);
    }

    private void checkDoesCaseExist(String searchField, CaseType caseType, SecurityDto securityDto) {
        Optional<ProbateCaseDetails> caseResponseOptional = coreCaseDataService
            .findCase(searchField, caseType, securityDto);
        if (caseResponseOptional.isPresent()) {
            throw new CaseAlreadyExistsException(searchField);
        }
    }

    private void assertIdentifierMatchesCase(String identifier, CaseData caseData, CaseType caseType) {
        if (!caseType.equals(CaseType.GRANT_OF_REPRESENTATION)) {
            Pair<String, String> searchFieldValuePair = searchFieldFactory.getSearchFieldValuePair(caseType, caseData);
            String searchFieldValueInBody = searchFieldValuePair.getRight();
            if (!searchFieldValueInBody.equals(identifier)) {
                throw new AssertFieldException(ValidationErrorResponse.builder()
                    .errors(Lists.newArrayList(ValidationError.builder()
                        .field(searchFieldValuePair.getLeft())
                        .message("Path variable identifier must match identifier in form")
                        .build()))
                    .build());
            }
        }
    }
}
