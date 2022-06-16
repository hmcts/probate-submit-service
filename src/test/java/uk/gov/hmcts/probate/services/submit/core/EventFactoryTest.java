package uk.gov.hmcts.probate.services.submit.core;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.probate.model.cases.CaseEvents;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.probate.model.cases.CaseType.CAVEAT;
import static uk.gov.hmcts.reform.probate.model.cases.CaseType.GRANT_OF_REPRESENTATION;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_CREATE_APPLICATION;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_CREATE_CASE;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_CREATE_DRAFT;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_PAYMENT_FAILED;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_PAYMENT_FAILED_AGAIN;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_PAYMENT_FAILED_TO_SUCCESS;
import static uk.gov.hmcts.reform.probate.model.cases.EventId.GOP_UPDATE_DRAFT;

public class EventFactoryTest {

    private EventFactory eventFactory;

    private CaseEvents caseEvents;

    @BeforeEach
    public void setUp() {
        caseEvents = CaseEvents.builder()
                .createCaseApplicationEventId(GOP_CREATE_APPLICATION)
                .createCaseEventId(GOP_CREATE_CASE)
                .createDraftEventId(GOP_CREATE_DRAFT)
                .paymentFailedAgainEventId(GOP_PAYMENT_FAILED_AGAIN)
                .paymentFailedEventId(GOP_PAYMENT_FAILED)
                .paymentFailedToSuccessEventId(GOP_PAYMENT_FAILED_TO_SUCCESS)
                .updateDraftEventId(GOP_UPDATE_DRAFT)
                .build();

        Map<CaseType, CaseEvents> eventsMap = ImmutableMap.<CaseType, CaseEvents>builder()
                .put(GRANT_OF_REPRESENTATION, caseEvents)
                .build();

        eventFactory = new EventFactory(eventsMap);
    }

    @Test
    public void shouldGetCaseEvents() {
        CaseEvents actualCaseEvents = eventFactory.getCaseEvents(GRANT_OF_REPRESENTATION);

        assertEquals(caseEvents, actualCaseEvents);
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenConfigDoesNotExistForType() {
        assertThrows(IllegalArgumentException.class, () -> {
            eventFactory.getCaseEvents(CAVEAT);
        });
    }
}
