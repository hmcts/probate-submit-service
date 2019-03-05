package uk.gov.hmcts.probate.services.submit.core;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.probate.model.cases.CaseEvents;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EventFactory {

    private final Map<CaseType, CaseEvents> eventsMap;

    public CaseEvents getCaseEvents(CaseType caseType) {
        return Optional.ofNullable(eventsMap.get(caseType)).orElseThrow(IllegalArgumentException::new);
    }
}
