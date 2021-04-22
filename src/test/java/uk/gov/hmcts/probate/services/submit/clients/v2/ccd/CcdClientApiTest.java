package uk.gov.hmcts.probate.services.submit.clients.v2.ccd;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.probate.security.SecurityDto;
import uk.gov.hmcts.probate.services.submit.core.SearchFieldFactory;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.client.model.UserId;
import uk.gov.hmcts.reform.probate.model.cases.ApplicationType;
import uk.gov.hmcts.reform.probate.model.cases.CaseData;
import uk.gov.hmcts.reform.probate.model.cases.CaseInfo;
import uk.gov.hmcts.reform.probate.model.cases.CaseState;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;
import uk.gov.hmcts.reform.probate.model.cases.EventId;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantOfRepresentationData;
import uk.gov.hmcts.reform.probate.model.cases.grantofrepresentation.GrantType;

import java.time.LocalDateTime;
import java.util.List;
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

    public static final String INVITATION_ID = "12334345";
    public static final String INVIATION_QUERY = "inviationQuery";
    private static final Long CASE_ID = 123456789L;
    private static final CaseState STATE = CaseState.DRAFT;
    private static final String AUTHORIZATION = "AUTHXXXXXXXXXXXXX";
    private static final String SERVICE_AUTHORIZATION = "SERVAUTHZZZZZZZZZZZZZ";
    private static final String USER_ID = "33";
    private static final String TOKEN = "TOKENXXXX123466LLDJH";
    private static final String APPLICANT_EMAIL = "test@test.com";
    private static final String EMAIL_QUERY_PARAM = "case.primaryApplicantEmailAddress";
    private static final EventId CREATE_DRAFT = EventId.GOP_CREATE_DRAFT;
    private static final EventId UPDATE_DRAFT = EventId.GOP_UPDATE_DRAFT;
    private static final String PROBATE_DESCRIPTOR = "Probate application";
    @Rule
    public ExpectedException exception = ExpectedException.none();
    @Mock
    private CoreCaseDataApi mockCoreCaseDataApi;
    @Mock
    private CaseAccessApi mockCaseAccessApi;
    @Mock
    private CaseContentBuilder caseContentBuilder;
    @Mock
    private CaseResponseBuilder caseResponseBuilder;
    @Mock
    private SearchFieldFactory searchFieldFactory;
    @Mock
    private CcdElasticSearchQueryBuilder mockInvitationElasticSearchQueryBuilder;
    private CcdClientApi ccdClientApi;
    private SecurityDto securityDto;

    private CaseData caseData;

    private StartEventResponse startEventResponse;

    private CaseDetails caseDetails;

    private CaseDataContent caseDataContent;

    private String inviteField = "inviteField";

    private SearchResult searchResult;

    private ProbateCaseDetails probateCaseDetails;

    @Captor
    private ArgumentCaptor<UserId> userIdCaptor;

    @Before
    public void setUp() {
        ccdClientApi = new CcdClientApi(mockCoreCaseDataApi, mockCaseAccessApi, caseContentBuilder, caseResponseBuilder,
            searchFieldFactory, mockInvitationElasticSearchQueryBuilder);

        when(searchFieldFactory.getEsSearchFieldName(CaseType.GRANT_OF_REPRESENTATION))
            .thenReturn("primaryApplicantEmailAddress");

        when(searchFieldFactory.getSearchApplicantEmailFieldName()).thenReturn("primaryApplicantEmailAddress");

        String inviteField = "inviteField";

        when(searchFieldFactory.getSearchInviteFieldName()).thenReturn(inviteField);

        securityDto = SecurityDto.builder()
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
            .state(STATE.getName())
            .caseTypeId(GRANT_OF_REPRESENTATION.getName())
            .createdDate(LocalDateTime.now())
            .data(ImmutableMap.of("applicationType", ApplicationType.PERSONAL,
                "caseType", GrantType.INTESTACY))
            .build();

        caseDataContent = CaseDataContent.builder()
            .eventToken(TOKEN)
            .event(Event.builder()
                .id(CREATE_DRAFT.getName())
                .description(PROBATE_DESCRIPTOR)
                .summary(PROBATE_DESCRIPTOR)
                .build())
            .data(caseData)
            .build();

        searchResult = SearchResult.builder().cases(Lists.newArrayList(caseDetails)).build();
        CaseInfo caseInfo = new CaseInfo();
        caseInfo.setCaseId(caseDetails.getId().toString());
        caseInfo.setState(CaseState.getState(caseDetails.getState()));
        caseInfo.setCaseCreatedDate(
            caseDetails.getCreatedDate() != null ? caseDetails.getCreatedDate().toLocalDate() : null);

        probateCaseDetails = ProbateCaseDetails.builder()
            .caseData(caseData)
            .caseInfo(caseInfo)
            .build();

        when(caseResponseBuilder.createCaseResponse(caseDetails)).thenReturn(probateCaseDetails);


    }

    @Test
    public void shouldCreateCase() {
        when(mockCoreCaseDataApi.startForCitizen(AUTHORIZATION, SERVICE_AUTHORIZATION, USER_ID, PROBATE.name(),
            GRANT_OF_REPRESENTATION.getName(), CREATE_DRAFT.getName())).thenReturn(startEventResponse);

        when(mockCoreCaseDataApi
            .submitForCitizen(eq(AUTHORIZATION), eq(SERVICE_AUTHORIZATION), eq(USER_ID), eq(PROBATE.name()),
                eq(GRANT_OF_REPRESENTATION.getName()), eq(false), eq(caseDataContent))).thenReturn(caseDetails);
        when(caseContentBuilder.createCaseDataContent(caseData, CREATE_DRAFT, startEventResponse, PROBATE_DESCRIPTOR))
            .thenReturn(caseDataContent);
        ProbateCaseDetails caseResponse = ccdClientApi.createCase(caseData, CREATE_DRAFT, securityDto);

        assertThat(caseResponse, is(notNullValue()));
        assertThat(caseResponse.getCaseInfo().getCaseId(), is(CASE_ID.toString()));
        assertThat(caseResponse.getCaseInfo().getState(), is(STATE));
        verify(mockCoreCaseDataApi, times(1))
            .startForCitizen(AUTHORIZATION, SERVICE_AUTHORIZATION, USER_ID, PROBATE.name(),
                GRANT_OF_REPRESENTATION.getName(), CREATE_DRAFT.getName());
        verify(mockCoreCaseDataApi, times(1))
            .submitForCitizen(eq(AUTHORIZATION), eq(SERVICE_AUTHORIZATION), eq(USER_ID), eq(PROBATE.name()),
                eq(GRANT_OF_REPRESENTATION.getName()), eq(false), eq(caseDataContent));
    }

    @Test
    public void shouldUpdateCase() {
        caseDataContent.getEvent().setId(UPDATE_DRAFT.getName());

        when(mockCoreCaseDataApi.startEventForCitizen(AUTHORIZATION, SERVICE_AUTHORIZATION, USER_ID, PROBATE.name(),
            GRANT_OF_REPRESENTATION.getName(), CASE_ID.toString(), UPDATE_DRAFT.getName()))
            .thenReturn(startEventResponse);

        when(mockCoreCaseDataApi
            .submitEventForCitizen(eq(AUTHORIZATION), eq(SERVICE_AUTHORIZATION), eq(USER_ID), eq(PROBATE.name()),
                eq(GRANT_OF_REPRESENTATION.getName()), eq(CASE_ID.toString()), eq(false), eq(caseDataContent)))
            .thenReturn(caseDetails);
        when(caseContentBuilder.createCaseDataContent(caseData, UPDATE_DRAFT, startEventResponse, PROBATE_DESCRIPTOR))
            .thenReturn(caseDataContent);

        ProbateCaseDetails caseResponse =
            ccdClientApi.updateCase(CASE_ID.toString(), caseData, UPDATE_DRAFT, securityDto);

        assertThat(caseResponse, is(notNullValue()));
        assertThat(caseResponse.getCaseInfo().getCaseId(), is(CASE_ID.toString()));
        assertThat(caseResponse.getCaseInfo().getState(), is(STATE));
        verify(mockCoreCaseDataApi, times(1))
            .startEventForCitizen(AUTHORIZATION, SERVICE_AUTHORIZATION, USER_ID, PROBATE.name(),
                GRANT_OF_REPRESENTATION.getName(), CASE_ID.toString(), UPDATE_DRAFT.getName());
        verify(mockCoreCaseDataApi, times(1))
            .submitEventForCitizen(eq(AUTHORIZATION), eq(SERVICE_AUTHORIZATION), eq(USER_ID), eq(PROBATE.name()),
                eq(GRANT_OF_REPRESENTATION.getName()), eq(CASE_ID.toString()), eq(false), eq(caseDataContent));
    }

    @Test
    public void shouldUpdateCaseAsCaseWorker() {
        caseDataContent.getEvent().setId(UPDATE_DRAFT.getName());

        when(mockCoreCaseDataApi.startEventForCaseWorker(AUTHORIZATION, SERVICE_AUTHORIZATION, USER_ID, PROBATE.name(),
            GRANT_OF_REPRESENTATION.getName(), CASE_ID.toString(), UPDATE_DRAFT.getName()))
            .thenReturn(startEventResponse);

        when(mockCoreCaseDataApi
            .submitEventForCaseWorker(eq(AUTHORIZATION), eq(SERVICE_AUTHORIZATION), eq(USER_ID), eq(PROBATE.name()),
                eq(GRANT_OF_REPRESENTATION.getName()), eq(CASE_ID.toString()), eq(false), eq(caseDataContent)))
            .thenReturn(caseDetails);
        when(caseContentBuilder.createCaseDataContent(caseData, UPDATE_DRAFT, startEventResponse, PROBATE_DESCRIPTOR))
            .thenReturn(caseDataContent);

        ProbateCaseDetails caseResponse =
            ccdClientApi.updateCaseAsCaseworker(CASE_ID.toString(), caseData, UPDATE_DRAFT, securityDto);

        assertThat(caseResponse, is(notNullValue()));
        assertThat(caseResponse.getCaseInfo().getCaseId(), is(CASE_ID.toString()));
        assertThat(caseResponse.getCaseInfo().getState(), is(STATE));
        verify(mockCoreCaseDataApi, times(1))
            .startEventForCaseWorker(AUTHORIZATION, SERVICE_AUTHORIZATION, USER_ID, PROBATE.name(),
                GRANT_OF_REPRESENTATION.getName(), CASE_ID.toString(), UPDATE_DRAFT.getName());
        verify(mockCoreCaseDataApi, times(1))
            .submitEventForCaseWorker(eq(AUTHORIZATION), eq(SERVICE_AUTHORIZATION), eq(USER_ID), eq(PROBATE.name()),
                eq(GRANT_OF_REPRESENTATION.getName()), eq(CASE_ID.toString()), eq(false), eq(caseDataContent));
    }

    @Test
    public void shouldFindCase() {

        String queryString = "queryString";
        when(mockInvitationElasticSearchQueryBuilder.buildQuery(APPLICANT_EMAIL, "primaryApplicantEmailAddress"))
            .thenReturn(queryString);

        when(mockCoreCaseDataApi.searchCases(eq(AUTHORIZATION), eq(SERVICE_AUTHORIZATION),
            eq(GRANT_OF_REPRESENTATION.getName()), eq(queryString))).thenReturn(searchResult);


        Optional<ProbateCaseDetails> optionalCaseResponse =
            ccdClientApi.findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDto);

        ProbateCaseDetails caseResponse = optionalCaseResponse.get();
        assertThat(caseResponse, is(notNullValue()));
        assertThat(caseResponse.getCaseInfo().getCaseId(), is(CASE_ID.toString()));
        assertThat(caseResponse.getCaseInfo().getState(), is(STATE));
        verify(mockCoreCaseDataApi, times(1)).searchCases(eq(AUTHORIZATION), eq(SERVICE_AUTHORIZATION),
            eq(GRANT_OF_REPRESENTATION.getName()), eq(queryString));
    }

    @Test
    public void shouldFindCaseByApplicantEmail() {

        String queryString = "queryString";
        when(mockInvitationElasticSearchQueryBuilder.buildQuery(APPLICANT_EMAIL, "primaryApplicantEmailAddress"))
            .thenReturn(queryString);

        when(mockCoreCaseDataApi.searchCases(eq(AUTHORIZATION), eq(SERVICE_AUTHORIZATION),
            eq(GRANT_OF_REPRESENTATION.getName()), eq(queryString)))
            .thenReturn(SearchResult.builder().cases(Lists.newArrayList(caseDetails)).build());


        Optional<ProbateCaseDetails> optionalCaseResponse =
            ccdClientApi.findCaseByApplicantEmail(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDto);

        ProbateCaseDetails caseResponse = optionalCaseResponse.get();
        assertThat(caseResponse, is(notNullValue()));
        assertThat(caseResponse.getCaseInfo().getCaseId(), is(CASE_ID.toString()));
        assertThat(caseResponse.getCaseInfo().getState(), is(STATE));
        verify(mockCoreCaseDataApi, times(1)).searchCases(eq(AUTHORIZATION), eq(SERVICE_AUTHORIZATION),
            eq(GRANT_OF_REPRESENTATION.getName()), eq(queryString));
    }

    @Test
    public void shouldFindAllCase() {

        String queryString = "queryString";
        when(mockInvitationElasticSearchQueryBuilder.buildFindAllCasesQuery()).thenReturn(queryString);

        when(mockCoreCaseDataApi.searchCases(eq(AUTHORIZATION), eq(SERVICE_AUTHORIZATION),
            eq(GRANT_OF_REPRESENTATION.getName()), eq(queryString)))
            .thenReturn(SearchResult.builder().cases(Lists.newArrayList(caseDetails)).build());

        List<ProbateCaseDetails> results = ccdClientApi.findCases(GRANT_OF_REPRESENTATION, securityDto);

        CaseInfo caseInfo = results.stream().findFirst().get().getCaseInfo();
        assertThat(results, is(notNullValue()));
        assertThat(caseInfo.getCaseId(), is(CASE_ID.toString()));
        assertThat(caseInfo.getState(), is(STATE));
        verify(mockCoreCaseDataApi, times(1)).searchCases(eq(AUTHORIZATION), eq(SERVICE_AUTHORIZATION),
            eq(GRANT_OF_REPRESENTATION.getName()), eq(queryString));
    }

    @Test
    public void shouldFindCaseById() {
        when(mockCoreCaseDataApi.getCase(eq(AUTHORIZATION), eq(SERVICE_AUTHORIZATION), eq(CASE_ID.toString())))
            .thenReturn(caseDetails);

        Optional<ProbateCaseDetails> optionalCaseResponse = ccdClientApi.findCaseById(CASE_ID.toString(), securityDto);

        ProbateCaseDetails caseResponse = optionalCaseResponse.get();
        assertThat(caseResponse, is(notNullValue()));
        assertThat(caseResponse.getCaseInfo().getCaseId(), is(CASE_ID.toString()));
        assertThat(caseResponse.getCaseInfo().getState(), is(STATE));
        verify(mockCoreCaseDataApi, times(1))
            .getCase(eq(AUTHORIZATION), eq(SERVICE_AUTHORIZATION), eq(CASE_ID.toString()));
    }

    @Test
    public void shouldReturnEmptyOptionalWhenCaseNotFoundById() {
        when(mockCoreCaseDataApi.getCase(eq(AUTHORIZATION), eq(SERVICE_AUTHORIZATION), eq(CASE_ID.toString())))
            .thenReturn(null);

        Optional<ProbateCaseDetails> optionalCaseResponse = ccdClientApi.findCaseById(CASE_ID.toString(), securityDto);

        assertThat(optionalCaseResponse.isPresent(), is(false));
        verify(mockCoreCaseDataApi, times(1))
            .getCase(eq(AUTHORIZATION), eq(SERVICE_AUTHORIZATION), eq(CASE_ID.toString()));
    }

    @Test
    public void shouldFindCaseByInvitationId() {


        when(mockInvitationElasticSearchQueryBuilder.buildQuery(INVITATION_ID, inviteField))
            .thenReturn(INVIATION_QUERY);

        when(mockCoreCaseDataApi
            .searchCases(eq(AUTHORIZATION), eq(SERVICE_AUTHORIZATION), eq(GRANT_OF_REPRESENTATION.getName()),
                eq(INVIATION_QUERY))).thenReturn(SearchResult.builder().cases(Lists.newArrayList(caseDetails)).build());

        Optional<ProbateCaseDetails> optionalCaseResponse =
            ccdClientApi.findCaseByInviteId(INVITATION_ID, GRANT_OF_REPRESENTATION, securityDto);

        ProbateCaseDetails caseResponse = optionalCaseResponse.get();
        assertThat(caseResponse, is(notNullValue()));
        assertThat(caseResponse.getCaseInfo().getCaseId(), is(CASE_ID.toString()));
        assertThat(caseResponse.getCaseInfo().getState(), is(STATE));
        verify(mockCoreCaseDataApi, times(1))
            .searchCases(eq(AUTHORIZATION), eq(SERVICE_AUTHORIZATION), eq(GRANT_OF_REPRESENTATION.getName()),
                eq(INVIATION_QUERY));
    }


    @Test
    public void shouldReturnEmptyOptionalWhenReturningNullOnSearch() {
        String queryString = "queryString";
        when(mockInvitationElasticSearchQueryBuilder.buildQuery(APPLICANT_EMAIL, "primaryApplicantEmailAddress"))
            .thenReturn(queryString);

        when(mockCoreCaseDataApi.searchCases(eq(AUTHORIZATION), eq(SERVICE_AUTHORIZATION),
            eq(GRANT_OF_REPRESENTATION.getName()), eq(queryString)))
            .thenReturn(SearchResult.builder().cases(null).build());


        Optional<ProbateCaseDetails> optionalCaseResponse =
            ccdClientApi.findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDto);


        assertThat(optionalCaseResponse.isPresent(), is(false));
    }

    @Test
    public void shouldReturnEmptyOptionalWhenReturningNullOnSearchInvitation() {
        String invitationId = INVITATION_ID;
        String invitationQuery =
            INVIATION_QUERY;
        when(mockInvitationElasticSearchQueryBuilder.buildQuery(invitationId, inviteField)).thenReturn(invitationQuery);

        when(mockCoreCaseDataApi
            .searchCases(eq(AUTHORIZATION), eq(SERVICE_AUTHORIZATION), eq(GRANT_OF_REPRESENTATION.getName()),
                eq(invitationQuery))).thenReturn(SearchResult.builder().cases(null).build());

        Optional<ProbateCaseDetails> optionalCaseResponse =
            ccdClientApi.findCaseByInviteId(invitationId, GRANT_OF_REPRESENTATION, securityDto);

        assertThat(optionalCaseResponse.isPresent(), is(false));
    }

    @Test
    public void shouldThrowIllegalStateExceptionWhenFindingMoreThanOneCase() {
        exception.expect(IllegalStateException.class);
        exception.expectMessage("Multiple cases exist with applicant email provided!");

        String queryString = "queryString";
        when(mockInvitationElasticSearchQueryBuilder.buildQuery(APPLICANT_EMAIL, "primaryApplicantEmailAddress"))
            .thenReturn(queryString);

        when(mockCoreCaseDataApi.searchCases(eq(AUTHORIZATION), eq(SERVICE_AUTHORIZATION),
            eq(GRANT_OF_REPRESENTATION.getName()), eq(queryString)))
            .thenReturn(SearchResult.builder().cases(Lists.newArrayList(caseDetails, caseDetails)).build());


        ccdClientApi.findCase(APPLICANT_EMAIL, GRANT_OF_REPRESENTATION, securityDto);
    }
}
