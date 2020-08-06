package uk.gov.hmcts.probate.functional.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class IdamData {

    private String email;

    private String forename;

    private String surname;

    private String password;

    @JsonProperty("user_group_name")
    private String userGroupName;

    private List<Role> roles;
}