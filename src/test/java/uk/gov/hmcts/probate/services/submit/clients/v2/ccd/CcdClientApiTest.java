package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.probate.security.SecurityDTO;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.probate.model.cases.ApplicationType;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.EventId;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantOfRepresentationData;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantType;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.probate.model.cases.CaseType.GRANT_OF_REPRESENTATION;
import static uk.gov.hmcts.reform.probate.model.cases.JurisdictionId.PROBATE;

@RunWith(MockitoJUnitRunner.class)
public class CcdClientApiTest {

    private static final Long CASE_ID = 123456789L;

    private static final String STATE = "Draft";

    private static final String AUTHORIZATION = "AUTHXXXXXXXXXXXXX";

    private static final String SERVICE_AUTHORIZATION = "SERVAUTHZZZZZZZZZZZZZ";

    private static final String USER_ID = "33";

    private static final String TOKEN = "TOKENXXXX123466LLDJH";

    private static final String APPLICANT_EMAIL = "test@test.com";

    private static final String EMAIL_QUERY_PARAM = "case.primaryApplicantEmailAddress";

    private static final EventId CREATE_DRAFT = GRANT_OF_REPRESENTATION.getCaseEvents().getCreateDraftEventId();

    private static final EventId UPDATE_DRAFT = GRANT_OF_REPRESENTATION.getCaseEvents().getUpdateDraftEventId();

    @Mock
    private CoreCaseDataApi mockCoreCaseDataApi;

    @InjectMocks
    private CcdClientApi ccdClientApi;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private SecurityDTO securityDTO;

    private CaseData caseData;

    private StartEventResponse startEventResponse;

    private CaseDetails caseDetails;

    private CaseDataContent caseDataContent;

    @Before
    public void setUp() {
        ccdClientApi = new CcdClientApi(mockCoreCaseDataApi, new CaseDetailsToCaseDataMapper(new ObjectMapper()));

        securityDTO = SecurityDTO.builder()
                .authorisation(AUTHORIZATION)
                .serviceAuthorisation(SERVICE_AUTHORIZATION)
                .userId(USER_ID)
                .build();

        caseData = new GrantOfRepresentationData();
        startEventResponse = StartEventResponse.builder()
                .token(TOKEN)
                .build();

        caseDetails = CaseDetails.builder()
                .id(CASE_ID)
                .state(STATE)
                .caseTypeId(GRANT_OF_REPRESENTATION.getName())
                .data(ImmutableMap.of("applicationType", ApplicationType.PERSONAL,
                        "caseType", GrantType.INTESTACY))
                .build();

        caseDataContent = CaseDataContent.builder()
                .eventToken(TOKEN)
                .event(Event.builder()
                        .id(CREATE_DRAFT.getName())
                        .description("Probate application")
                        .summary("Probate application")
                        .build())
                .data(caseData)
                .build();
    }

    @Test
    public void shouldCreateCase() {
        when(mockCoreCaseDataApi.startForCitizen(AUTHORIZATION, SERVICE_AUTHORIZATION, USER_ID, PROBATE.name(),
                GRANT_OF_REPRESENTATION.getName(), CREATE_DRAFT.getName())).thenReturn(startEventResponse);

        when(mockCoreCaseDataApi.submitForCitizen(eq(AUTHORIZATION), eq(SERVICE_AUTHORIZATION), eq(USER_ID), eq(PROBATE.name()),
                eq(GRANT_OF_REPRESENTATION.getName()), eq(false), eq(caseDataContent))).thenReturn(caseDetails);

        ProbateCaseDetails caseResponse = ccdClientApi.createCase(caseData, CREATE_DRAFT, securityDTO);

        assertThat(caseResponse, is(notNullValue()));
        assertThat(caseResponse.getCaseInfo().getCaseId(), is(CASE_ID.toString()));
        assertThat(caseResponse.getCaseInfo().getState(), is(STATE));
        verify(mockCoreCaseDataApi, times(1)).startForCitizen(AUTHORIZATION, SERVICE_AUTHORIZATION, USER_ID, PROBATE.name(),
                GRANT_OF_REPRESENTATION.getName(), CREATE_DRAFT.getName());
        verify(mockCoreCaseDataApi, times(1)).submitForCitizen(eq(AUTHORIZATION), eq(SERVICE_AUTHORIZATION), eq(USER_ID), eq(PROBATE.name()),
                eq(GRANT_OF_REPRESENTATION.getName()), eq(false), eq(caseDataContent));
    }

    @Test
    public void shouldUpdateCase() {
        caseDataContent.getEvent().setId(UPDATE_DRAFT.getName());

        when(mockCoreCaseDataApi.startEventForCitizen(AUTHORIZATION, SERVICE_AUTHORIZATION, USER_ID, PROBATE.name(),
                GRANT_OF_REPRESENTATION.getName(), CASE_ID.toString(), UPDATE_DRAFT.getName())).thenReturn(startEventResponse);

        when(mockCoreCaseDataApi.submitEventForCitizen(eq(AUTHORIZATION), eq(SERVICE_AUTHORIZATION), eq(USER_ID), eq(PROBATE.name()),
                eq(GRANT_OF_REPRESENTATION.getName()), eq(CASE_ID.toString()), eq(false), eq(caseDataContent))).thenReturn(caseDetails);

        ProbateCaseDetails caseResponse = ccdClientApi.updateCase(CASE_ID.toString(), caseData, UPDATE_DRAFT, securityDTO);

        assertThat(caseResponse, is(notNullValue()));
        assertThat(caseResponse.getCaseInfo().getCaseId(), is(CASE_ID.toString()));
        assertThat(caseResponse.getCaseInfo().getState(), is(STATE));
        verify(mockCoreCaseDataApi, times(1)).startEventForCitizen(AUTHORIZATION, SERVICE_AUTHORIZATION, USER_ID, PROBATE.name(),
                GRANT_OF_REPRESENTATION.getName(), CASE_ID.toString(), UPDATE_DRAFT.getName());
        verify(mockCoreCaseDataApi, times(1)).submitEventForCitizen(eq(AUTHORIZATION), eq(SERVICE_AUTHORIZATION), eq(USER_ID), eq(PROBATE.name()),
                eq(GRANT_OF_REPRESENTATION.getName()), eq(CASE_ID.toString()), eq(false), eq(caseDataContent));
    }

    @Test
    public void shouldFindCase() {
        Map<String, String> queryMap = ImmutableMap.of(EMAIL_QUERY_PARAM, APPLICANT_EMAIL);
        when(mockCoreCaseDataApi.searchForCitizen(eq(AUTHORIZATION), eq(SERVICE_AUTHORIZATION), eq(USER_ID), eq(PROBATE.name()),
                eq(GRANT_OF_REPRESENTATION.getName()), eq(queryMap))).thenReturn(Lists.newArrayList(caseDetails));

        Optional<ProbateCaseDetails> optionalCaseResponse = ccdClientApi.findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDTO);

        ProbateCaseDetails caseResponse = optionalCaseResponse.get();
        assertThat(caseResponse, is(notNullValue()));
        assertThat(caseResponse.getCaseInfo().getCaseId(), is(CASE_ID.toString()));
        assertThat(caseResponse.getCaseInfo().getState(), is(STATE));
        verify(mockCoreCaseDataApi, times(1)).searchForCitizen(eq(AUTHORIZATION), eq(SERVICE_AUTHORIZATION), eq(USER_ID), eq(PROBATE.name()),
                eq(GRANT_OF_REPRESENTATION.getName()), eq(queryMap));
    }

    @Test
    public void shouldReturnEmptyOptionalWhenReturningNullOnSearch() {
        Map<String, String> queryMap = ImmutableMap.of(EMAIL_QUERY_PARAM, APPLICANT_EMAIL);
        when(mockCoreCaseDataApi.searchForCitizen(eq(AUTHORIZATION), eq(SERVICE_AUTHORIZATION), eq(USER_ID), eq(PROBATE.name()),
                eq(GRANT_OF_REPRESENTATION.getName()), eq(queryMap))).thenReturn(null);

        Optional<ProbateCaseDetails> optionalCaseResponse = ccdClientApi.findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDTO);

        assertThat(optionalCaseResponse.isPresent(), is(false));
    }

    @Test
    public void shouldThrowIllegalStateExceptionWhenFindingMoreThanOneCase() {
        exception.expect(IllegalStateException.class);
        exception.expectMessage("Multiple cases exist with applicant email provided!");

        Map<String, String> queryMap = ImmutableMap.of(EMAIL_QUERY_PARAM, APPLICANT_EMAIL);
        when(mockCoreCaseDataApi.searchForCitizen(eq(AUTHORIZATION), eq(SERVICE_AUTHORIZATION), eq(USER_ID), eq(PROBATE.name()),
                eq(GRANT_OF_REPRESENTATION.getName()), eq(queryMap))).thenReturn(Lists.newArrayList(caseDetails, caseDetails));

        ccdClientApi.findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDTO);
    }
}
