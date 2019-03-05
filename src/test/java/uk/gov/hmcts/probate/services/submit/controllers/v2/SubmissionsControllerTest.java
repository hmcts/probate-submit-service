package uk.gov.hmcts.probate.services.submit.controllers.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.probate.services.submit.services.SubmissionsService;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CaseInfo;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.SubmitResult;
import uk.gov.hmcts.reform.probate.model.cases.ValidatorResults;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = {SubmissionsController.class}, secure = false)
public class SubmissionsControllerTest {

    private static final String SUBMISSIONS_URL = "/submissions/update";
    private static final String EMAIL_ADDRESS = "test@test.com";
    private static final String CASE_ID = "1343242352";
    private static final String APPLICATION_CREATED = "ApplicationCreated";

    @MockBean
    private SubmissionsService submissionsService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shouldUpdateDraftToCase() throws Exception {
        String json = TestUtils.getJSONFromFile("files/v2/intestacyGrantOfRepresentation.json");
        CaseData grantOfRepresentation = objectMapper.readValue(json, CaseData.class);
        CaseInfo caseInfo = new CaseInfo();
        caseInfo.setCaseId(CASE_ID);
        caseInfo.setState(APPLICATION_CREATED);
        ProbateCaseDetails caseResponse = ProbateCaseDetails.builder().caseInfo(caseInfo).caseData(grantOfRepresentation).build();
        ProbateCaseDetails caseRequest = ProbateCaseDetails.builder().caseData(grantOfRepresentation).build();
        ValidatorResults validatorResults = new ValidatorResults();
        when(submissionsService.updateDraftToCase(eq(EMAIL_ADDRESS), eq(caseRequest))).thenReturn(new SubmitResult(caseResponse, validatorResults));

        mockMvc.perform(post(SUBMISSIONS_URL + "/" + EMAIL_ADDRESS)
                .content(objectMapper.writeValueAsString(caseRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(submissionsService).updateDraftToCase(eq(EMAIL_ADDRESS), eq(caseRequest));
    }

    @Test
    public void shouldReturn400OnSubmitOfInvalidJson() throws Exception {
        String json = TestUtils.getJSONFromFile("files/v2/intestacyGrantOfRepresentationInvalid.json");
        CaseData grantOfRepresentation = objectMapper.readValue(json, CaseData.class);
        CaseInfo caseInfo = new CaseInfo();
        caseInfo.setCaseId(CASE_ID);
        caseInfo.setState(APPLICATION_CREATED);
        ProbateCaseDetails caseResponse = ProbateCaseDetails.builder().caseInfo(caseInfo).caseData(grantOfRepresentation).build();
        ProbateCaseDetails caseRequest = ProbateCaseDetails.builder().caseData(grantOfRepresentation).build();
        MvcResult result  = mockMvc.perform(post(SUBMISSIONS_URL + "/" + EMAIL_ADDRESS)
                .content(objectMapper.writeValueAsString(caseRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        System.out.println(content);
    }

}
