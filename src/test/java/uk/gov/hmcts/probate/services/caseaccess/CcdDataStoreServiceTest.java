package uk.gov.hmcts.probate.services.caseaccess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.probate.model.CaseUser;
import uk.gov.hmcts.probate.model.FindUserCaseRolesRequest;
import uk.gov.hmcts.probate.model.FindUserCaseRolesResponse;
import uk.gov.hmcts.probate.security.SecurityUtils;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class CcdDataStoreServiceTest {

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private CaseRoleClient caseRoleClient;

    private CcdDataStoreService ccdDataStoreService;

    @Test
    void shouldReturnTrueWhenCurrentUserHasCreatorCaseRole() {
        ccdDataStoreService = new CcdDataStoreService(securityUtils, caseRoleClient);

        String caseId = "1234abcd";
        String userToken = "Bearer user-token";
        String serviceToken = "Bearer service-token";
        String userId = "user-123";

        when(securityUtils.getUserToken()).thenReturn(userToken);
        when(securityUtils.generateServiceToken()).thenReturn(serviceToken);
        when(securityUtils.getUserId()).thenReturn(userId);

        CaseUser creatorUser = mock(CaseUser.class);
        when(creatorUser.getCaseRole()).thenReturn("[SOME_OTHER_ROLE]");
        CaseUser otherUser = mock(CaseUser.class);
        when(otherUser.getCaseRole()).thenReturn("[CREATOR]");

        FindUserCaseRolesResponse response = mock(FindUserCaseRolesResponse.class);
        when(response.getCaseUsers()).thenReturn(List.of(creatorUser, otherUser));

        when(caseRoleClient.findUserCaseRoles(
                eq(userToken),
                eq(serviceToken),
                any(FindUserCaseRolesRequest.class)
        )).thenReturn(response);

        boolean result = ccdDataStoreService.isCaseOwner(caseId);

        assertThat(result).isTrue();

        ArgumentCaptor<FindUserCaseRolesRequest> requestCaptor =
                ArgumentCaptor.forClass(FindUserCaseRolesRequest.class);
        verify(caseRoleClient).findUserCaseRoles(eq(userToken), eq(serviceToken), requestCaptor.capture());
        assertThat(requestCaptor.getValue().getCaseIds()).containsExactly(caseId.toLowerCase());
        assertThat(requestCaptor.getValue().getUserIds()).containsExactly(userId);
    }

    @Test
    void shouldReturnFalseWhenCurrentUserDoesNotHaveCreatorCaseRole() {
        ccdDataStoreService = new CcdDataStoreService(securityUtils, caseRoleClient);

        String caseId = "1234abcd";
        String userToken = "Bearer user-token";
        String serviceToken = "Bearer service-token";
        String userId = "user-123";

        when(securityUtils.getUserToken()).thenReturn(userToken);
        when(securityUtils.generateServiceToken()).thenReturn(serviceToken);
        when(securityUtils.getUserId()).thenReturn(userId);

        CaseUser caseworkerUser = mock(CaseUser.class);
        when(caseworkerUser.getCaseRole()).thenReturn("[CASEWORKER]");
        CaseUser otherUser = mock(CaseUser.class);
        when(otherUser.getCaseRole()).thenReturn("[SOME_OTHER_ROLE]");

        FindUserCaseRolesResponse response = mock(FindUserCaseRolesResponse.class);
        when(response.getCaseUsers()).thenReturn(List.of(caseworkerUser, otherUser));

        when(caseRoleClient.findUserCaseRoles(
                eq(userToken),
                eq(serviceToken),
                any(FindUserCaseRolesRequest.class)
        )).thenReturn(response);

        boolean result = ccdDataStoreService.isCaseOwner(caseId);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenNoCaseUsersAreReturned() {
        ccdDataStoreService = new CcdDataStoreService(securityUtils, caseRoleClient);

        String caseId = "1234abcd";
        String userToken = "Bearer user-token";
        String serviceToken = "Bearer service-token";
        String userId = "user-123";

        when(securityUtils.getUserToken()).thenReturn(userToken);
        when(securityUtils.generateServiceToken()).thenReturn(serviceToken);
        when(securityUtils.getUserId()).thenReturn(userId);

        FindUserCaseRolesResponse response = mock(FindUserCaseRolesResponse.class);
        when(response.getCaseUsers()).thenReturn(List.of());

        when(caseRoleClient.findUserCaseRoles(
                eq(userToken),
                eq(serviceToken),
                any(FindUserCaseRolesRequest.class)
        )).thenReturn(response);

        boolean result = ccdDataStoreService.isCaseOwner(caseId);

        assertThat(result).isFalse();
    }

    @Test
    void shouldBuildRequestWithProvidedCaseIdAndCurrentUserIdWhenFindingUserCaseRoles() {
        ccdDataStoreService = new CcdDataStoreService(securityUtils, caseRoleClient);

        String caseId = "CASE-ABC-123";
        String userToken = "Bearer user-token";
        String serviceToken = "Bearer service-token";
        String userId = "user-123";

        FindUserCaseRolesResponse expectedResponse = mock(FindUserCaseRolesResponse.class);

        when(securityUtils.getUserToken()).thenReturn(userToken);
        when(securityUtils.generateServiceToken()).thenReturn(serviceToken);
        when(securityUtils.getUserId()).thenReturn(userId);
        when(caseRoleClient.findUserCaseRoles(
                eq(userToken),
                eq(serviceToken),
                any(FindUserCaseRolesRequest.class)
        )).thenReturn(expectedResponse);

        FindUserCaseRolesResponse actualResponse = ccdDataStoreService.findUserCaseRoles(caseId);

        assertThat(actualResponse).isSameAs(expectedResponse);

        ArgumentCaptor<FindUserCaseRolesRequest> requestCaptor =
                ArgumentCaptor.forClass(FindUserCaseRolesRequest.class);
        verify(caseRoleClient).findUserCaseRoles(eq(userToken), eq(serviceToken), requestCaptor.capture());
        assertThat(requestCaptor.getValue().getCaseIds()).containsExactly(caseId);
        assertThat(requestCaptor.getValue().getUserIds()).containsExactly(userId);
    }

    @Test
    void shouldReturnTrueWhenCaseDetailsIsNull() {
        ccdDataStoreService = new CcdDataStoreService(securityUtils, caseRoleClient);

        boolean result = ccdDataStoreService.isCasesOwner(null);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnTrueWhenCaseDetailsIsEmpty() {
        ccdDataStoreService = new CcdDataStoreService(securityUtils, caseRoleClient);

        boolean result = ccdDataStoreService.isCasesOwner(List.of());

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnTrueWhenAllRequestedCasesHaveCreatorRole() {
        ccdDataStoreService = new CcdDataStoreService(securityUtils, caseRoleClient);

        String userToken = "Bearer user-token";
        String serviceToken = "Bearer service-token";
        String userId = "user-123";

        ProbateCaseDetails details1 = mock(ProbateCaseDetails.class);
        uk.gov.hmcts.reform.probate.model.cases.CaseInfo caseInfo1 =
                mock(uk.gov.hmcts.reform.probate.model.cases.CaseInfo.class);
        when(details1.getCaseInfo()).thenReturn(caseInfo1);
        when(caseInfo1.getCaseId()).thenReturn("case-1");

        ProbateCaseDetails details2 = mock(ProbateCaseDetails.class);
        uk.gov.hmcts.reform.probate.model.cases.CaseInfo caseInfo2 =
                mock(uk.gov.hmcts.reform.probate.model.cases.CaseInfo.class);
        when(details2.getCaseInfo()).thenReturn(caseInfo2);
        when(caseInfo2.getCaseId()).thenReturn("case-2");

        CaseUser creatorCase1 = mock(CaseUser.class);
        when(creatorCase1.getCaseId()).thenReturn("case-1");
        when(creatorCase1.getCaseRole()).thenReturn("[CREATOR]");

        CaseUser creatorCase2 = mock(CaseUser.class);
        when(creatorCase2.getCaseId()).thenReturn("case-2");
        when(creatorCase2.getCaseRole()).thenReturn("[CREATOR]");

        FindUserCaseRolesResponse response = mock(FindUserCaseRolesResponse.class);
        when(response.getCaseUsers()).thenReturn(List.of(creatorCase1, creatorCase2));

        when(securityUtils.getUserToken()).thenReturn(userToken);
        when(securityUtils.generateServiceToken()).thenReturn(serviceToken);
        when(securityUtils.getUserId()).thenReturn(userId);
        when(caseRoleClient.findUserCaseRoles(
                eq(userToken),
                eq(serviceToken),
                any(FindUserCaseRolesRequest.class)
        )).thenReturn(response);

        boolean result = ccdDataStoreService.isCasesOwner(List.of(details1, details2));

        assertThat(result).isTrue();

        ArgumentCaptor<FindUserCaseRolesRequest> requestCaptor =
                ArgumentCaptor.forClass(FindUserCaseRolesRequest.class);
        verify(caseRoleClient).findUserCaseRoles(eq(userToken), eq(serviceToken), requestCaptor.capture());
        assertThat(requestCaptor.getValue().getCaseIds()).containsExactly("case-1", "case-2");
        assertThat(requestCaptor.getValue().getUserIds()).containsExactly(userId);
    }

    @Test
    void shouldReturnFalseWhenAnyRequestedCaseDoesNotHaveCreatorRole() {
        ccdDataStoreService = new CcdDataStoreService(securityUtils, caseRoleClient);

        String userToken = "Bearer user-token";
        String serviceToken = "Bearer service-token";
        String userId = "user-123";

        ProbateCaseDetails details1 = mock(ProbateCaseDetails.class);
        uk.gov.hmcts.reform.probate.model.cases.CaseInfo caseInfo1 =
                mock(uk.gov.hmcts.reform.probate.model.cases.CaseInfo.class);
        when(details1.getCaseInfo()).thenReturn(caseInfo1);
        when(caseInfo1.getCaseId()).thenReturn("case-1");

        ProbateCaseDetails details2 = mock(ProbateCaseDetails.class);
        uk.gov.hmcts.reform.probate.model.cases.CaseInfo caseInfo2 =
                mock(uk.gov.hmcts.reform.probate.model.cases.CaseInfo.class);
        when(details2.getCaseInfo()).thenReturn(caseInfo2);
        when(caseInfo2.getCaseId()).thenReturn("case-2");

        CaseUser creatorCase1 = mock(CaseUser.class);
        when(creatorCase1.getCaseId()).thenReturn("case-1");
        when(creatorCase1.getCaseRole()).thenReturn("[CREATOR]");

        CaseUser nonCreatorCase2 = mock(CaseUser.class);
        when(nonCreatorCase2.getCaseId()).thenReturn("case-2");
        when(nonCreatorCase2.getCaseRole()).thenReturn("[CASEWORKER]");

        FindUserCaseRolesResponse response = mock(FindUserCaseRolesResponse.class);
        when(response.getCaseUsers()).thenReturn(List.of(creatorCase1, nonCreatorCase2));

        when(securityUtils.getUserToken()).thenReturn(userToken);
        when(securityUtils.generateServiceToken()).thenReturn(serviceToken);
        when(securityUtils.getUserId()).thenReturn(userId);
        when(caseRoleClient.findUserCaseRoles(
                eq(userToken),
                eq(serviceToken),
                any(FindUserCaseRolesRequest.class)
        )).thenReturn(response);

        boolean result = ccdDataStoreService.isCasesOwner(List.of(details1, details2));

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenARequestedCaseIsMissingFromReturnedCaseUsers() {
        ccdDataStoreService = new CcdDataStoreService(securityUtils, caseRoleClient);

        String userToken = "Bearer user-token";
        String serviceToken = "Bearer service-token";
        String userId = "user-123";

        ProbateCaseDetails details1 = mock(ProbateCaseDetails.class);
        uk.gov.hmcts.reform.probate.model.cases.CaseInfo caseInfo1 =
                mock(uk.gov.hmcts.reform.probate.model.cases.CaseInfo.class);
        when(details1.getCaseInfo()).thenReturn(caseInfo1);
        when(caseInfo1.getCaseId()).thenReturn("case-1");

        ProbateCaseDetails details2 = mock(ProbateCaseDetails.class);
        uk.gov.hmcts.reform.probate.model.cases.CaseInfo caseInfo2 =
                mock(uk.gov.hmcts.reform.probate.model.cases.CaseInfo.class);
        when(details2.getCaseInfo()).thenReturn(caseInfo2);
        when(caseInfo2.getCaseId()).thenReturn("case-2");

        CaseUser creatorCase1 = mock(CaseUser.class);
        when(creatorCase1.getCaseId()).thenReturn("case-1");
        when(creatorCase1.getCaseRole()).thenReturn("[CREATOR]");

        FindUserCaseRolesResponse response = mock(FindUserCaseRolesResponse.class);
        when(response.getCaseUsers()).thenReturn(List.of(creatorCase1));

        when(securityUtils.getUserToken()).thenReturn(userToken);
        when(securityUtils.generateServiceToken()).thenReturn(serviceToken);
        when(securityUtils.getUserId()).thenReturn(userId);
        when(caseRoleClient.findUserCaseRoles(
                eq(userToken),
                eq(serviceToken),
                any(FindUserCaseRolesRequest.class)
        )).thenReturn(response);

        boolean result = ccdDataStoreService.isCasesOwner(List.of(details1, details2));

        assertThat(result).isFalse();
    }
}