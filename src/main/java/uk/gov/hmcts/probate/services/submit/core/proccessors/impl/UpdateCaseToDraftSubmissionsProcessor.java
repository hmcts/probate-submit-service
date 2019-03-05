package uk.gov.hmcts.probate.services.submit.core.proccessors.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.probate.security.SecurityDTO;
import uk.gov.hmcts.probate.security.SecurityUtils;
import uk.gov.hmcts.probate.services.submit.core.EventFactory;
import uk.gov.hmcts.probate.services.submit.core.SearchFieldFactory;
import uk.gov.hmcts.probate.services.submit.core.proccessors.AbstractSubmissionsProcessor;
import uk.gov.hmcts.probate.services.submit.model.v2.exception.CaseStatePreconditionException;
import uk.gov.hmcts.probate.services.submit.services.CoreCaseDataService;
import uk.gov.hmcts.probate.services.submit.validation.CaseDataValidatorFactory;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CaseEvents;
import uk.gov.hmcts.reform.probate.model.cases.CaseState;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.EventId;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;

@Slf4j
@Component
public class UpdateCaseToDraftSubmissionsProcessor extends AbstractSubmissionsProcessor {

    private final CoreCaseDataService coreCaseDataService;
    private final EventFactory eventFactory;

    @Autowired
    public UpdateCaseToDraftSubmissionsProcessor(CoreCaseDataService coreCaseDataService, EventFactory eventFactory, SecurityUtils securityUtils, SearchFieldFactory searchFieldFactory, CaseDataValidatorFactory caseDataValidatorFactory) {
        super(securityUtils, searchFieldFactory, caseDataValidatorFactory, coreCaseDataService);
        this.coreCaseDataService = coreCaseDataService;
        this.eventFactory = eventFactory;
    }

    @Override
    protected ProbateCaseDetails processCase(String identifier, CaseData caseData, CaseType caseType, SecurityDTO securityDTO) {
        ProbateCaseDetails caseResponse = findCase(identifier, CaseType.getCaseType(caseData), securityDTO);
        log.info("Found case with case Id: {}", caseResponse.getCaseInfo().getCaseId());
        CaseState state = CaseState.getState(caseResponse.getCaseInfo().getState());
        CaseEvents caseEvents = eventFactory.getCaseEvents(caseType);
        checkStatePrecondition(state, caseEvents.getCreateCaseApplicationEventId());
        String caseId = caseResponse.getCaseInfo().getCaseId();
        return coreCaseDataService.updateCase(caseId, caseData, caseEvents.getCreateCaseApplicationEventId(), securityDTO);
    }


    private void checkStatePrecondition(CaseState caseState, EventId eventId) {
        if (!caseState.equals(CaseState.DRAFT)) {
            throw new CaseStatePreconditionException(caseState, eventId);
        }
    }
}
