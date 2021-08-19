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
                                                 StartEventResponse startEventResponse, String eventDescriptor) {
        String description = caseData.getDescription();
        log.info("============================== createCaseDataContent description is => ");
        log.info(description);
        if (description == null) {
            log.info("description  is null");
            description = eventDescriptor;
        }else if(description == "null"){
            log.info("description is strring null");
            description = eventDescriptor;
        }

        log.info("description to put in the case event ===========> ");
        log.info(description);
                                                   
        return CaseDataContent.builder()
            .event(createEvent(eventId, eventDescriptor, description))
            .eventToken(startEventResponse.getToken())
            .data(caseData)
            .build();
    }

    private Event createEvent(EventId eventId, String eventDescriptor, String description) {
        return Event.builder()
            .id(eventId.getName())
            .description(description)
            .summary(eventDescriptor)
            .build();
    }
}
