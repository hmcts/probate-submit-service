package uk.gov.hmcts.probate.functional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.probate.functional.model.IdamData;
import uk.gov.hmcts.probate.functional.model.Role;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;

import java.util.Arrays;
import java.util.Base64;

import static io.restassured.RestAssured.given;

@Component
public class TestTokenGenerator {

    @Value("${idam.oauth2.client.id}")
    private String clientId;

    @Value("${idam.oauth2.redirect_uri}")
    private String redirectUri;

    @Value("${idam.secret}")
    private String secret;

    @Value("${user.auth.provider.oauth2.url}")
    private String idamUserBaseUrl;

    @Value("${idam.username}")
    private String email;

    @Value("${idam.password}")
    private String password;

    @Autowired
    private ServiceAuthTokenGenerator tokenGenerator;

    public String generateServiceAuthorisation() {
        return tokenGenerator.generate();
    }

    public void createNewUser() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        IdamData idamData = IdamData.builder().email(email).forename("forename").surname("surname")
                .password(password).roles(Arrays.asList(Role.builder().code("citizen").build()))
                .build();

        given().headers("Content-type", "application/json")
                .relaxedHTTPSValidation()
                .body(objectMapper.writeValueAsString(idamData))
                .post(idamUserBaseUrl + "/testing-support/accounts");
    }

    public String generateAuthorisation() {
        return generateClientToken();
    }

    private String generateClientToken() {
        String code = generateClientCode();
        String token = RestAssured.given().post(idamUserBaseUrl + "/oauth2/token?" + "code=" + code +
                "&client_secret=" + secret +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&grant_type=authorization_code")
                .body().path("access_token");
        return token;
    }

    private String generateClientCode() {
        final String encoded = Base64.getEncoder().encodeToString((email + ":" + password).getBytes());
        return RestAssured.given().baseUri(idamUserBaseUrl)
                .header("Authorization", "Basic " + encoded)
                .post("/oauth2/authorize?response_type=code&client_id=" + clientId + "&redirect_uri=" + redirectUri)
                .body().path("code");

    }
}