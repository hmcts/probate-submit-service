package uk.gov.hmcts.probate.services.submit.core.proccessors.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.probate.security.SecurityDTO;
import uk.gov.hmcts.probate.security.SecurityUtils;
import uk.gov.hmcts.probate.services.submit.Registry;
import uk.gov.hmcts.probate.services.submit.core.EventFactory;
import uk.gov.hmcts.probate.services.submit.core.SearchFieldFactory;
import uk.gov.hmcts.probate.services.submit.core.proccessors.AbstractSubmissionsProcessor;
import uk.gov.hmcts.probate.services.submit.model.v2.exception.CaseAlreadyExistsException;
import uk.gov.hmcts.probate.services.submit.services.CoreCaseDataService;
import uk.gov.hmcts.probate.services.submit.services.SequenceService;
import uk.gov.hmcts.probate.services.submit.validation.CaseDataValidatorFactory;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CaseEvents;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.RegistryLocation;

import java.util.Optional;

@Slf4j
@Component
public class CreateCaseSubmissionsProcessor extends AbstractSubmissionsProcessor {

    private final CoreCaseDataService coreCaseDataService;
    private final EventFactory eventFactory;
    private final SequenceService sequenceService;

    @Autowired
    public CreateCaseSubmissionsProcessor(CoreCaseDataService coreCaseDataService, EventFactory eventFactory, SecurityUtils securityUtils, SearchFieldFactory searchFieldFactory,
                                          CaseDataValidatorFactory caseDataValidatorFactory, SequenceService sequenceService) {
        super(securityUtils, searchFieldFactory, caseDataValidatorFactory, coreCaseDataService);
        this.coreCaseDataService = coreCaseDataService;
        this.eventFactory = eventFactory;
        this.sequenceService = sequenceService;
    }

    @Override
    protected ProbateCaseDetails processCase(String identifier, CaseData caseData, CaseType caseType, SecurityDTO securityDTO) {
        checkDoesCaseExist(identifier, CaseType.getCaseType(caseData), securityDTO);
        log.info("Case not found with case Id: {}", identifier);
        CaseEvents caseEvents = eventFactory.getCaseEvents(caseType);
        Registry registry = sequenceService.identifyNextRegistry();
        caseData.setRegistryLocation(RegistryLocation.findRegistryLocationByName(registry.getName()));
        return coreCaseDataService.createCase(caseData, caseEvents.getCreateCaseApplicationEventId(), securityDTO);
    }

    private void checkDoesCaseExist(String searchField, CaseType caseType, SecurityDTO securityDTO) {
        Optional<ProbateCaseDetails> caseResponseOptional = coreCaseDataService.
                findCase(searchField, caseType, securityDTO);
        if (caseResponseOptional.isPresent()) {
            throw new CaseAlreadyExistsException(searchField);
        }
    }

}
