package uk.gov.hmcts.probate.services.submit.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.probate.services.submit.services.CasesService;
import uk.gov.hmcts.probate.services.submit.services.SubmissionsService;
import uk.gov.hmcts.reform.probate.model.cases.CaseInfo;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantOfRepresentationData;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
public class SubmissionsServiceImplTest {

    public static final String EMAIL_ADDRESS = "email address";

    @Mock
    private CreateCaseSubmissionsProcessor createCaseSubmissionsProcessor;

    @Mock
    private CasesService casesService;

    private SubmissionsService submissionsService;

    private ProbateCaseDetails probateCaseDetails;

    @BeforeEach
    public void setUp() {
        probateCaseDetails = ProbateCaseDetails.builder().caseData(GrantOfRepresentationData.builder().build())
                .caseInfo(CaseInfo.builder().build()).build();
        submissionsService = new SubmissionsServiceImpl(createCaseSubmissionsProcessor);
    }

    @Test
    public void shouldCallCreateCase() {
        submissionsService.createCase(EMAIL_ADDRESS, probateCaseDetails);
        verify(createCaseSubmissionsProcessor, times(1)).process(eq(EMAIL_ADDRESS), any());
    }
}
