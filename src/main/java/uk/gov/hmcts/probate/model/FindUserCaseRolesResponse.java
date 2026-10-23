package uk.gov.hmcts.probate.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.probate.model.CaseUser;

import java.util.List;

@Getter
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FindUserCaseRolesResponse {
    @JsonProperty("case_users")
    private List<CaseUser> caseUsers;
}
