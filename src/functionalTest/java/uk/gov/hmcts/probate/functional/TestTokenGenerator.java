package uk.gov.hmcts.probate.functional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.probate.functional.model.IdamData;
import uk.gov.hmcts.probate.functional.model.Role;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

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

    @Value("${idam.password}")
    private String password;

    @Autowired
    private ServiceAuthTokenGenerator tokenGenerator;

    private final Cache<String, String> cache = Caffeine.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).build();

    public String generateServiceAuthorisation() {
        return tokenGenerator.generate();
    }

    public void createNewUser(String email, String role) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        IdamData idamData = IdamData.builder().email(email).forename("forename").surname("surname")
            .password(password).roles(Arrays.asList(Role.builder().code(role).build()))
            .build();

        given().headers("Content-type", "application/json")
            .relaxedHTTPSValidation()
            .body(objectMapper.writeValueAsString(idamData))
            .post(idamUserBaseUrl + "/testing-support/accounts");
    }

    public String generateAuthorisation(String email) {
        return getCachedIdamOpenIdToken(email);
    }

    public String generateOpenIdToken(String email) {
        JsonPath jp = RestAssured.given().relaxedHTTPSValidation().post(idamUserBaseUrl + "/o/token?"
                        + "client_secret=" + secret
                        + "&client_id=" + clientId
                        + "&redirect_uri=" + redirectUri
                        + "&username=" + email
                        + "&password=" + password
                        + "&grant_type=password&scope=openid profile roles")
                .body().jsonPath();

        return jp.get("access_token");
    }

    private String getCachedIdamOpenIdToken(String email) {
        String userToken = cache.getIfPresent(email);
        if (userToken == null) {
            userToken = generateOpenIdToken(email);
            cache.put(email, userToken);
        }
        return userToken;
    }
}