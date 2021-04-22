package uk.gov.hmcts.probate.services.submit.controllers.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.probate.services.submit.model.v2.exception.CaseValidationException;
import uk.gov.hmcts.probate.services.submit.services.SubmissionsService;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CaseInfo;
import uk.gov.hmcts.reform.probate.model.cases.CaseState;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.SubmitResult;
import uk.gov.hmcts.reform.probate.model.cases.ValidatorResults;

import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Path;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SubmissionsControllerTest {

    private static final String SUBMISSIONS_URL = "/submissions";
    private static final String EMAIL_ADDRESS = "test@test.com";
    private static final String CASE_ID = "1343242352";
    private static final String APPLICATION_CREATED = "ApplicationCreated";

    @MockBean
    private SubmissionsService submissionsService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void shouldCreateCase() throws Exception {
        String json = TestUtils.getJsonFromFile("files/v2/intestacyGrantOfRepresentation.json");
        CaseData grantOfRepresentation = objectMapper.readValue(json, CaseData.class);
        CaseInfo caseInfo = new CaseInfo();
        caseInfo.setCaseId(CASE_ID);
        caseInfo.setState(CaseState.PA_APP_CREATED);
        ProbateCaseDetails caseResponse =
            ProbateCaseDetails.builder().caseInfo(caseInfo).caseData(grantOfRepresentation).build();
        ProbateCaseDetails caseRequest = ProbateCaseDetails.builder().caseData(grantOfRepresentation).build();
        ValidatorResults validatorResults = new ValidatorResults(Lists.newArrayList());
        when(submissionsService.createCase(eq(EMAIL_ADDRESS), eq(caseRequest)))
            .thenReturn(new SubmitResult(caseResponse, validatorResults));

        mockMvc.perform(post(SUBMISSIONS_URL + "/" + EMAIL_ADDRESS)
            .content(objectMapper.writeValueAsString(caseRequest))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(submissionsService).createCase(eq(EMAIL_ADDRESS), eq(caseRequest));
    }

    @Test
    public void shouldThrowBadRequestOnCreateCaseWithInvalidPayload() throws Exception {
        String json = TestUtils.getJsonFromFile("files/v2/intestacyGrantOfRepresentation.json");
        CaseData grantOfRepresentation = objectMapper.readValue(json, CaseData.class);
        CaseInfo caseInfo = new CaseInfo();
        caseInfo.setCaseId(CASE_ID);
        caseInfo.setState(CaseState.PA_APP_CREATED);
        ProbateCaseDetails caseResponse =
            ProbateCaseDetails.builder().caseInfo(caseInfo).caseData(grantOfRepresentation).build();

        ConstraintViolation<CaseData> constraintViolation = Mockito.mock(ConstraintViolation.class);
        when(constraintViolation.getMessage()).thenReturn("must not be null");
        Path path = Mockito.mock(Path.class);
        when(path.toString()).thenReturn("fieldName");
        when(constraintViolation.getPropertyPath()).thenReturn(path);

        Set<ConstraintViolation<CaseData>> constraintViolations = new HashSet<>();
        constraintViolations.add(constraintViolation);
        CaseValidationException caseValidationException = new CaseValidationException(constraintViolations);

        ProbateCaseDetails caseRequest = ProbateCaseDetails.builder().caseData(grantOfRepresentation).build();
        when(submissionsService.createCase(eq(EMAIL_ADDRESS), eq(caseRequest))).thenThrow(caseValidationException);

        mockMvc.perform(post(SUBMISSIONS_URL + "/" + EMAIL_ADDRESS)
            .content(objectMapper.writeValueAsString(caseRequest))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0].field", is("fieldName")))
            .andExpect(jsonPath("$.errors[0].message", is("must not be null")))
            .andExpect(status().isBadRequest());
        verify(submissionsService).createCase(eq(EMAIL_ADDRESS), eq(caseRequest));
    }

}
