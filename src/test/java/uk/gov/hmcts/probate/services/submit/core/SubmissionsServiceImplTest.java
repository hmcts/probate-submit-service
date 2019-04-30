package uk.gov.hmcts.probate.services.submit.core;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.probate.services.submit.core.proccessors.impl.CreateCaseSubmissionsProcessor;
import uk.gov.hmcts.probate.services.submit.core.proccessors.impl.UpdateCaseToDraftSubmissionsProcessor;
import uk.gov.hmcts.probate.services.submit.services.CasesService;
import uk.gov.hmcts.probate.services.submit.services.SubmissionsService;
import uk.gov.hmcts.reform.probate.model.cases.CaseInfo;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantOfRepresentationData;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SubmissionsServiceImplTest {

    public static final String EMAIL_ADDRESS = "email address";

    @Mock
    private CreateCaseSubmissionsProcessor createCaseSubmissionsProcessor;

    @Mock
    private UpdateCaseToDraftSubmissionsProcessor updateCaseToDraftSubmissionsProcessor;

    @Mock
    private CasesService casesService;

    private SubmissionsService submissionsService;

    private ProbateCaseDetails probateCaseDetails;

    @Before
    public void setUp() {
        probateCaseDetails = ProbateCaseDetails.builder().caseData(GrantOfRepresentationData.builder().build())
                .caseInfo(CaseInfo.builder().build()).build();
        submissionsService = new SubmissionsServiceImpl(updateCaseToDraftSubmissionsProcessor, createCaseSubmissionsProcessor, casesService);
    }

    @Test
    public void shouldCallCreateCase() {
        submissionsService.createCase(EMAIL_ADDRESS, probateCaseDetails);
        verify(createCaseSubmissionsProcessor, times(1)).process(eq(EMAIL_ADDRESS), any());
        verify(updateCaseToDraftSubmissionsProcessor, never()).process(EMAIL_ADDRESS, () -> probateCaseDetails);
    }

    @Test
    public void shouldCallUpdateDraftToCase() {
        submissionsService.updateDraftToCase(EMAIL_ADDRESS, CaseType.GRANT_OF_REPRESENTATION);
        verify(updateCaseToDraftSubmissionsProcessor, times(1)).process(eq(EMAIL_ADDRESS), any());
    }
}
