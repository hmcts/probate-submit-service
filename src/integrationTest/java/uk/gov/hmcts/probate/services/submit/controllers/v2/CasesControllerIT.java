package uk.gov.hmcts.probate.services.submit.controllers.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.probate.security.SecurityUtils;
import uk.gov.hmcts.probate.services.caseaccess.CcdDataStoreService;
import uk.gov.hmcts.probate.services.submit.model.v2.exception.CaseValidationException;
import uk.gov.hmcts.probate.services.submit.services.CasesService;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CaseInfo;
import uk.gov.hmcts.reform.probate.model.cases.CaseState;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantOfRepresentationData;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantType;
import uk.gov.hmcts.reform.probate.model.client.AssertFieldException;
import uk.gov.hmcts.reform.probate.model.client.ValidationErrorResponse;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class CasesControllerIT {

    private static final String CASES_URL = "/cases";
    private static final String CASES_BY_APPLICANT_EMAIL_URL = "/cases/applicantEmail";
    private static final String CASES_INITATE_URL = "/cases/initiate";
    private static final String CASES_ALL_URL = "/cases/all";
    private static final String CASES_CASEWORKER_URL = "/cases/caseworker";
    private static final String CASES_INVITATION_URL = "/cases/invitation";
    private static final String EMAIL_ADDRESS = "test@test.com";
    private static final String INVITATION_ID = "invitationId";
    private static final String CASE_ID = "1343242352";
    private static final String VALIDATE_ENDPOINT = "/validations";

    @MockitoBean
    private CasesService casesService;

    @MockitoBean
    private SecurityUtils securityUtils;

    @MockitoBean
    private CcdDataStoreService ccdDataStoreService;

    @MockitoBean(name = "serviceAuthTokenGenerator")
    private AuthTokenGenerator serviceAuthTokenGenerator;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setup() {

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        when(serviceAuthTokenGenerator.generate()).thenReturn("mock-s2s-token");
    }

    @Test
    public void shouldGetCaseForIntestacyGrantOfRepresentation() throws Exception {
        when(securityUtils.getUserId()).thenReturn("test-citizen-user-id");
        when(ccdDataStoreService.isCaseOwner(EMAIL_ADDRESS)).thenReturn(true);
        String json = TestUtils.getJsonFromFile("files/v2/intestacyGrantOfRepresentation.json");
        CaseData grantOfRepresentation = objectMapper.readValue(json, CaseData.class);
        CaseInfo caseInfo = new CaseInfo();
        caseInfo.setCaseId(CASE_ID);
        caseInfo.setState(CaseState.DRAFT);
        ProbateCaseDetails caseResponse =
            ProbateCaseDetails.builder().caseInfo(caseInfo).caseData(grantOfRepresentation).build();
        when(casesService.getCase(EMAIL_ADDRESS, CaseType.GRANT_OF_REPRESENTATION)).thenReturn(caseResponse);

        mockMvc.perform(get(CASES_URL + "/" + EMAIL_ADDRESS)
            .header("ServiceAuthorization", "Bearer dummy-s2s-token")
            .header("Authorization", "Bearer dummy-user-token")
            .with(user("test-user@email.com").authorities(new SimpleGrantedAuthority("citizen")))
            .param("caseType", CaseType.GRANT_OF_REPRESENTATION.name())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(casesService, times(1)).getCase(EMAIL_ADDRESS, CaseType.GRANT_OF_REPRESENTATION);
    }

    @Test
    public void shouldGetCaseByApplicantEmailForIntestacyGrantOfRepresentation() throws Exception {
        when(securityUtils.getUserId()).thenReturn("test-citizen-user-id");
        when(ccdDataStoreService.isCaseOwner(CASE_ID)).thenReturn(true);
        String json = TestUtils.getJsonFromFile("files/v2/intestacyGrantOfRepresentation.json");
        CaseData grantOfRepresentation = objectMapper.readValue(json, CaseData.class);
        CaseInfo caseInfo = new CaseInfo();
        caseInfo.setCaseId(CASE_ID);
        caseInfo.setState(CaseState.DRAFT);
        ProbateCaseDetails caseResponse =
            ProbateCaseDetails.builder().caseInfo(caseInfo).caseData(grantOfRepresentation).build();
        when(casesService.getCaseByApplicantEmail(EMAIL_ADDRESS, CaseType.GRANT_OF_REPRESENTATION))
                .thenReturn(caseResponse);

        mockMvc.perform(get(CASES_BY_APPLICANT_EMAIL_URL + "/" + EMAIL_ADDRESS)
            .header("ServiceAuthorization", "Bearer dummy-s2s-token")
            .header("Authorization", "Bearer dummy-user-token")
            .with(user("test-user@email.com").authorities(new SimpleGrantedAuthority("citizen")))
            .param("caseType", CaseType.GRANT_OF_REPRESENTATION.name())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(casesService, times(1)).getCaseByApplicantEmail(EMAIL_ADDRESS, CaseType.GRANT_OF_REPRESENTATION);
    }


    @Test
    public void shouldGetAllCaseForGrantOfRepresentation() throws Exception {
        when(securityUtils.getUserId()).thenReturn("test-citizen-user-id");
        when(ccdDataStoreService.isCasesOwner(anyList())).thenReturn(true);
        String json = TestUtils.getJsonFromFile("files/v2/intestacyGrantOfRepresentation.json");
        CaseData grantOfRepresentation = objectMapper.readValue(json, CaseData.class);
        CaseInfo caseInfo = new CaseInfo();
        caseInfo.setCaseId(CASE_ID);
        caseInfo.setState(CaseState.DRAFT);
        ProbateCaseDetails caseResponse =
            ProbateCaseDetails.builder().caseInfo(caseInfo).caseData(grantOfRepresentation).build();
        when(casesService.getAllCases(CaseType.GRANT_OF_REPRESENTATION)).thenReturn(Arrays.asList(caseResponse));

        mockMvc.perform(get(CASES_ALL_URL)
            .header("ServiceAuthorization", "Bearer dummy-s2s-token")
            .header("Authorization", "Bearer dummy-user-token")
            .with(user("test-user@email.com").authorities(new SimpleGrantedAuthority("citizen")))
            .param("caseType", CaseType.GRANT_OF_REPRESENTATION.name())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(casesService, times(1)).getAllCases(CaseType.GRANT_OF_REPRESENTATION);
    }

    @Test
    public void shouldGetCaseByIdForIntestacyGrantOfRepresentation() throws Exception {
        when(securityUtils.getUserId()).thenReturn("test-citizen-user-id");
        when(ccdDataStoreService.isCaseOwner(CASE_ID)).thenReturn(true);
        String json = TestUtils.getJsonFromFile("files/v2/intestacyGrantOfRepresentation.json");
        CaseData grantOfRepresentation = objectMapper.readValue(json, CaseData.class);
        CaseInfo caseInfo = new CaseInfo();
        caseInfo.setCaseId(CASE_ID);
        caseInfo.setState(CaseState.DRAFT);
        ProbateCaseDetails caseResponse =
            ProbateCaseDetails.builder().caseInfo(caseInfo).caseData(grantOfRepresentation).build();
        when(casesService.getCaseById(CASE_ID)).thenReturn(caseResponse);

        mockMvc.perform(get(CASES_URL)
            .header("ServiceAuthorization", "Bearer dummy-s2s-token")
            .header("Authorization", "Bearer dummy-user-token")
            .with(user("test-user@email.com").authorities(new SimpleGrantedAuthority("citizen")))
            .param("caseId", CASE_ID)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(casesService, times(1)).getCaseById(CASE_ID);
    }

    @Test
    public void shouldGetCaseForIntestacyGrantOfRepresentationByInvitationId() throws Exception {

        ProbateCaseDetails probateCaseDetails = ProbateCaseDetails.builder().build();
        when(casesService.getCaseByInvitationId(INVITATION_ID, CaseType.GRANT_OF_REPRESENTATION))
            .thenReturn(probateCaseDetails);

        mockMvc.perform(get(CASES_INVITATION_URL + "/" + INVITATION_ID)
            .header("ServiceAuthorization", "Bearer dummy-s2s-token")
            .header("Authorization", "Bearer dummy-user-token")
            .with(user("test-user@email.com").authorities(new SimpleGrantedAuthority("caseworker-probate")))
            .param("caseType", CaseType.GRANT_OF_REPRESENTATION.name())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(casesService, times(1)).getCaseByInvitationId(INVITATION_ID, CaseType.GRANT_OF_REPRESENTATION);
    }


    @Test
    public void shouldValidate() throws Exception {
        when(securityUtils.getUserId()).thenReturn("test-citizen-user-id");
        when(ccdDataStoreService.isCaseOwner(EMAIL_ADDRESS)).thenReturn(true);

        CaseData caseData = GrantOfRepresentationData.builder().grantType(GrantType.GRANT_OF_PROBATE).build();

        mockMvc.perform(put(CASES_URL + "/" + EMAIL_ADDRESS + VALIDATE_ENDPOINT)
            .header("ServiceAuthorization", "Bearer dummy-s2s-token")
            .header("Authorization", "Bearer dummy-user-token")
            .with(user("test-user@email.com").authorities(new SimpleGrantedAuthority("citizen")))
            .param("caseType", CaseType.GRANT_OF_REPRESENTATION.name())
            .content(objectMapper.writeValueAsString(caseData))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(casesService, times(1)).validate(EMAIL_ADDRESS, CaseType.GRANT_OF_REPRESENTATION);
    }

    @Test
    public void shouldReturnStatusOf400WhenCaseValidationException() throws Exception {
        ConstraintViolation<CaseData> constraintViolation = Mockito.mock(ConstraintViolation.class);
        when(constraintViolation.getMessage()).thenReturn("must not be null");
        Path path = Mockito.mock(Path.class);
        when(path.toString()).thenReturn("fieldName");
        when(constraintViolation.getPropertyPath()).thenReturn(path);

        Set<ConstraintViolation<CaseData>> constraintViolations = new HashSet<>();
        constraintViolations.add(constraintViolation);
        CaseValidationException caseValidationException = new CaseValidationException(constraintViolations);
        CaseData caseData = GrantOfRepresentationData.builder().grantType(GrantType.GRANT_OF_PROBATE).build();
        ProbateCaseDetails probateCaseDetails = ProbateCaseDetails.builder().caseData(caseData).build();

        when(casesService.validate(EMAIL_ADDRESS, CaseType.GRANT_OF_REPRESENTATION)).thenThrow(caseValidationException);

        mockMvc.perform(put(CASES_URL + "/" + EMAIL_ADDRESS + VALIDATE_ENDPOINT)
            .header("ServiceAuthorization", "Bearer dummy-s2s-token")
            .header("Authorization", "Bearer dummy-user-token")
            .with(user("test-user@email.com").authorities(new SimpleGrantedAuthority("citizen")))
            .param("caseType", CaseType.GRANT_OF_REPRESENTATION.name())
            .content(objectMapper.writeValueAsString(probateCaseDetails))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0].field", is("fieldName")))
            .andExpect(jsonPath("$.errors[0].message", is("must not be null")))
            .andExpect(status().isBadRequest());

        verify(casesService, times(1)).validate(EMAIL_ADDRESS, CaseType.GRANT_OF_REPRESENTATION);
    }

    @Test
    public void shouldSaveCase() throws Exception {
        when(securityUtils.getUserId()).thenReturn("test-citizen-user-id");
        when(ccdDataStoreService.isCaseOwner(EMAIL_ADDRESS)).thenReturn(true);
        CaseData caseData = GrantOfRepresentationData.builder().grantType(GrantType.GRANT_OF_PROBATE).build();
        ProbateCaseDetails probateCaseDetails = ProbateCaseDetails.builder().caseData(caseData).build();

        mockMvc.perform(post(CASES_URL + "/" + EMAIL_ADDRESS)
            .header("ServiceAuthorization", "Bearer dummy-s2s-token")
            .header("Authorization", "Bearer dummy-user-token")
            .with(user("test-user@email.com").authorities(new SimpleGrantedAuthority("citizen")))
            .content(objectMapper.writeValueAsString(probateCaseDetails))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(casesService, times(1)).saveCase(anyString(), any(ProbateCaseDetails.class), anyString());
    }


    @Test
    public void shouldInitiateCase() throws Exception {
        CaseData caseData = GrantOfRepresentationData.builder().grantType(GrantType.GRANT_OF_PROBATE).build();
        ProbateCaseDetails probateCaseDetails = ProbateCaseDetails.builder().caseData(caseData).build();

        mockMvc.perform(post(CASES_INITATE_URL)
            .header("ServiceAuthorization", "Bearer dummy-s2s-token")
            .header("Authorization", "Bearer dummy-user-token")
            .with(user("test-user@email.com").authorities(new SimpleGrantedAuthority("citizen")))
            .content(objectMapper.writeValueAsString(probateCaseDetails))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(casesService, times(1)).initiateCase(any(ProbateCaseDetails.class));
    }

    @Test
    public void shouldSaveCaseAsCaseworker() throws Exception {
        CaseData caseData = GrantOfRepresentationData.builder().grantType(GrantType.GRANT_OF_PROBATE).build();
        ProbateCaseDetails probateCaseDetails = ProbateCaseDetails.builder().caseData(caseData).build();

        mockMvc.perform(post(CASES_CASEWORKER_URL + "/" + EMAIL_ADDRESS)
            .header("ServiceAuthorization", "Bearer dummy-s2s-token")
            .header("Authorization", "Bearer dummy-user-token")
            .with(user("test-user@email.com").authorities(new SimpleGrantedAuthority("caseworker-probate")))
            .content(objectMapper.writeValueAsString(probateCaseDetails))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(casesService, times(1)).saveCaseAsCaseworker(anyString(), any(ProbateCaseDetails.class));
    }

    @Test
    public void shouldGiveStatusOf401WhenAssertFieldExceptionThrown() throws Exception {
        when(securityUtils.getUserId()).thenReturn("test-citizen-user-id");
        when(ccdDataStoreService.isCaseOwner(EMAIL_ADDRESS)).thenReturn(true);
        CaseData caseData = GrantOfRepresentationData.builder().grantType(GrantType.GRANT_OF_PROBATE).build();
        ProbateCaseDetails probateCaseDetails = ProbateCaseDetails.builder().caseData(caseData).build();
        when(casesService.saveCase(anyString(), any(ProbateCaseDetails.class), anyString()))
            .thenThrow(new AssertFieldException(ValidationErrorResponse.builder().build()));
        mockMvc.perform(post(CASES_URL + "/" + EMAIL_ADDRESS)
            .with(user("test-user@email.com").authorities(new SimpleGrantedAuthority("citizen")))
            .content(objectMapper.writeValueAsString(probateCaseDetails))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());

        verify(casesService, times(1)).saveCase(anyString(), any(ProbateCaseDetails.class), anyString());
    }
}
