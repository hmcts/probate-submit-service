package uk.gov.hmcts.probate.services.caseaccess;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.probate.model.FindUserCaseRolesRequest;
import uk.gov.hmcts.probate.model.FindUserCaseRolesResponse;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(name = "case-role-client", url = "${core_case_data.api.url}")
public interface CaseRoleClient {
    static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @PostMapping(
            value = "/case-users/search",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    FindUserCaseRolesResponse findUserCaseRoles(
            @RequestHeader(AUTHORIZATION) String authorisation,
            @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
            @RequestBody final FindUserCaseRolesRequest request
    );

}