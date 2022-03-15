package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.EventId;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseContentBuilder {

    public CaseDataContent createCaseDataContent(CaseData caseData, EventId eventId,
                                                 StartEventResponse startEventResponse, 
                                                 String eventSummary, String eventDescription) {
        return CaseDataContent.builder()
            .event(createEvent(eventId, eventSummary, eventDescription))
            .eventToken(startEventResponse.getToken())
            .data(caseData)
            .build();
    }

    private Event createEvent(EventId eventId, String eventSummary, String eventDescription) {
        if (eventDescription == null || eventDescription.equals("null")) {
            eventDescription = eventSummary;
        }
        return Event.builder()
            .id(eventId.getName())
            .description(eventDescription)
            .summary(eventSummary)
            .build();
    }
}
