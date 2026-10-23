package uk.gov.hmcts.probate.services.caseaccess;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.probate.model.CaseUser;
import uk.gov.hmcts.probate.model.FindUserCaseRolesRequest;
import uk.gov.hmcts.probate.model.FindUserCaseRolesResponse;
import uk.gov.hmcts.probate.security.SecurityUtils;
import uk.gov.hmcts.reform.probate.model.cases.ProbateCaseDetails;

import java.util.List;

@Component("ccdDataStoreService")
@Slf4j
@RequiredArgsConstructor
public class CcdDataStoreService {

    private final SecurityUtils securityUtils;
    private final CaseRoleClient caseRoleClient;

    public boolean isCaseOwner(String caseId) {
        if (caseId == null || caseId.isEmpty()) {
            return true;
        }
        log.info("isCaseOwner caseId:: {}", caseId.toLowerCase());
        FindUserCaseRolesResponse response = findUserCaseRoles(caseId.toLowerCase());
        return response.getCaseUsers().stream()
                .anyMatch(caseUser -> "[CREATOR]".equals(caseUser.getCaseRole()));
    }

    public boolean isCasesOwner(List<ProbateCaseDetails> caseDetails) {
        if (caseDetails == null || caseDetails.isEmpty()) {
            return true;
        }

        List<String> caseIds = caseDetails.stream()
                .map(details -> details.getCaseInfo().getCaseId())
                .toList();

        List<CaseUser> matchedCaseUsers = findUserCaseRolesForMultipleCases(caseIds).getCaseUsers().stream()
                .filter(caseUser -> caseIds.contains(caseUser.getCaseId()))
                .toList();

        boolean allCasesReturned = matchedCaseUsers.stream()
                .map(CaseUser::getCaseId)
                .distinct()
                .count() == caseIds.size();

        boolean allCreator = matchedCaseUsers.stream()
                .allMatch(caseUser -> "[CREATOR]".equals(caseUser.getCaseRole()));

        return allCasesReturned && allCreator;
    }

    public FindUserCaseRolesResponse findUserCaseRoles(String caseId) {

        return caseRoleClient.findUserCaseRoles(
                securityUtils.getUserToken(),
                securityUtils.generateServiceToken(),
            buildFindUserCaseRolesRequest(caseId,securityUtils.getUserId())
        );
    }

    public FindUserCaseRolesResponse findUserCaseRolesForMultipleCases(List<String> caseIds) {

        return caseRoleClient.findUserCaseRoles(
                securityUtils.getUserToken(),
                securityUtils.generateServiceToken(),
                buildFindUserCaseRolesRequestForMultipleCases(caseIds,securityUtils.getUserId())
        );
    }

    private FindUserCaseRolesRequest buildFindUserCaseRolesRequest(String caseId, String userId) {
        return FindUserCaseRolesRequest
            .builder()
            .caseIds(List.of(caseId))
            .userIds(List.of(userId))
            .build();
    }

    private FindUserCaseRolesRequest buildFindUserCaseRolesRequestForMultipleCases(List<String> caseIds,
                                                                                   String userId) {
        return FindUserCaseRolesRequest
                .builder()
                .caseIds(caseIds)
                .userIds(List.of(userId))
                .build();
    }
}
