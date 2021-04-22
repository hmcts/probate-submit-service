package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseResponseBuilderTest {

    private CaseResponseBuilder caseResponseBuilder;

    @Mock
    private CaseDetailsToCaseDataMapper caseDetailsToCaseDataMapper;

    @Mock
    private CaseDetails caseDetails;
    @Mock
    private CaseData caseData;
    
    private static final Long CASE_ID = 12345678909L;
    private static final String STATE_NAME = "CaseCreated";

    @Before
    public void setup() {
        caseResponseBuilder = new CaseResponseBuilder(caseDetailsToCaseDataMapper);

        when(caseDetailsToCaseDataMapper.map(caseDetails)).thenReturn(caseData);
        when(caseDetails.getId()).thenReturn(CASE_ID);
        when(caseDetails.getState()).thenReturn(STATE_NAME);

    }

    @Test
    public void shouldCreateResponseWithDate() {
        LocalDateTime localDateTime = LocalDateTime.now();
        final LocalDate localDate = localDateTime.toLocalDate();
        when(caseDetails.getCreatedDate()).thenReturn(localDateTime);

        ProbateCaseDetails probateCaseDetails = caseResponseBuilder.createCaseResponse(caseDetails);
        assertThat(probateCaseDetails, is(notNullValue()));
        assertThat(probateCaseDetails.getCaseInfo(), is(notNullValue()));
        assertThat(probateCaseDetails.getCaseInfo().getCaseId(), is(CASE_ID.toString()));
        assertThat(probateCaseDetails.getCaseInfo().getState().getName(), is(STATE_NAME));
        assertThat(probateCaseDetails.getCaseInfo().getCaseCreatedDate(), is(localDate));
    }
    
    @Test
    public void shouldCreateResponseWithoutDate() {
        ProbateCaseDetails probateCaseDetails = caseResponseBuilder.createCaseResponse(caseDetails);
        assertThat(probateCaseDetails, is(notNullValue()));
        assertThat(probateCaseDetails.getCaseInfo(), is(notNullValue()));
        assertThat(probateCaseDetails.getCaseInfo().getCaseId(), is(CASE_ID.toString()));
        assertThat(probateCaseDetails.getCaseInfo().getState().getName(), is(STATE_NAME));
        assertThat(probateCaseDetails.getCaseInfo().getCaseCreatedDate(), is(nullValue()));
    }

}