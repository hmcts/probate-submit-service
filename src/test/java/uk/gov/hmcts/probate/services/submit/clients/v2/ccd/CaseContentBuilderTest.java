package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.EventId;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseContentBuilderTest {

    private static final String DESCRIPTOR = "Descriptor";
    private static final String EVENT_TOKEN = "Event token";
    private CaseContentBuilder caseContentBuilder;
    @Mock
    private CaseData caseData;
    @Mock
    private StartEventResponse startEventResponse;
    private EventId eventId;

    @Before
    public void setup() {
        caseContentBuilder = new CaseContentBuilder();
        eventId = EventId.GOP_CREATE_APPLICATION;

        when(startEventResponse.getToken()).thenReturn(EVENT_TOKEN);

    }

    @Test
    public void shouldAddContent() {

        CaseDataContent caseDataContent =
            caseContentBuilder.createCaseDataContent(caseData, eventId, startEventResponse, DESCRIPTOR);
        assertThat(caseDataContent, is(notNullValue()));
        assertThat(caseDataContent.getCaseReference(), is(nullValue()));
        assertThat(caseDataContent.getEventToken(), is(EVENT_TOKEN));
        assertThat(caseDataContent.getEvent(), is(notNullValue()));
        assertThat(caseDataContent.getEvent().getId(), is(eventId.getName()));
        assertThat(caseDataContent.getEvent().getDescription(), is(DESCRIPTOR));
        assertThat(caseDataContent.getEvent().getSummary(), is(DESCRIPTOR));
        assertThat(caseDataContent.getData(), is(caseData));
        assertThat(caseDataContent.getSecurityClassification(), is(nullValue()));

    }
}