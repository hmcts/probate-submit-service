package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.EventId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class CaseContentBuilderTest {

    private static final String DESCRIPTOR = "Descriptor";
    private static final String EVENT_TOKEN = "Event token";
    private CaseContentBuilder caseContentBuilder;
    @Mock
    private CaseData caseData;
    @Mock
    private StartEventResponse startEventResponse;
    private EventId eventId;

    @BeforeEach
    public void setup() {
        caseContentBuilder = new CaseContentBuilder();
        eventId = EventId.GOP_CREATE_APPLICATION;

        when(startEventResponse.getToken()).thenReturn(EVENT_TOKEN);

    }

    @Test
    public void shouldAddContent() {

        CaseDataContent caseDataContent =
            caseContentBuilder.createCaseDataContent(caseData, eventId, startEventResponse, DESCRIPTOR, DESCRIPTOR);
        assertNotNull(caseDataContent);
        assertNull(caseDataContent.getCaseReference());

        assertEquals(EVENT_TOKEN, caseDataContent.getEventToken());
        assertNotNull(caseDataContent.getEvent());
        assertEquals(eventId.getName(), caseDataContent.getEvent().getId());
        assertEquals(DESCRIPTOR, caseDataContent.getEvent().getDescription());
        assertEquals(DESCRIPTOR, caseDataContent.getEvent().getSummary());
        assertEquals(caseData, caseDataContent.getData());
        assertNull(caseDataContent.getSecurityClassification());

    }
}
