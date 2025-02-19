package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
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

    @BeforeEach
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
        when(caseDetails.getLastModified()).thenReturn(localDateTime);

        ProbateCaseDetails probateCaseDetails = caseResponseBuilder.createCaseResponse(caseDetails);
        assertNotNull(probateCaseDetails);
        assertNotNull(probateCaseDetails.getCaseInfo());
        assertEquals(CASE_ID.toString(), probateCaseDetails.getCaseInfo().getCaseId());
        assertEquals(STATE_NAME, probateCaseDetails.getCaseInfo().getState().getName());
        assertEquals(localDate, probateCaseDetails.getCaseInfo().getCaseCreatedDate());
        assertEquals(localDate, probateCaseDetails.getCaseInfo().getLastModifiedDate());
    }

    @Test
    public void shouldCreateResponseWithoutDate() {
        ProbateCaseDetails probateCaseDetails = caseResponseBuilder.createCaseResponse(caseDetails);
        assertNotNull(probateCaseDetails);
        assertNotNull(probateCaseDetails.getCaseInfo());
        assertEquals(CASE_ID.toString(), probateCaseDetails.getCaseInfo().getCaseId());
        assertEquals(STATE_NAME, probateCaseDetails.getCaseInfo().getState().getName());
        assertNull(probateCaseDetails.getCaseInfo().getCaseCreatedDate());
    }

}
