package uk.gov.hmcts.probate.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.restassured.RestAssured;
import io.restassured.response.ResponseBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.probate.contract.model.ClientAuthorizationCodeResponse;
import uk.gov.hmcts.probate.contract.model.ClientAuthorizationResponse;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;

import java.io.IOException;
import java.util.Base64;

import static io.restassured.RestAssured.given;

@Component
@Slf4j
@RequiredArgsConstructor
public class SolCcdServiceAuthTokenGenerator {

    String clientToken;
    @Value("${idam.oauth2.client.id}")
    private String clientId;
    @Value("${idam.oauth2.client.secret}")
    private String clientSecret;
    @Value("${idam.oauth2.redirect_uri}")
    private String redirectUri;
    @Value("${service.name}")
    private String serviceName;
    @Value("${service.auth.provider.base.url}")
    private String baseServiceAuthUrl;
    @Value("${idam.username}")
    private String idamUsername;

    @Value("${idam.userpassword}")
    private String idamPassword;

    @Value("${env}")
    private String environment;

    @Value("${idam.secret}")
    private String secret;

    @Value("${user.auth.provider.oauth2.url}")
    private String idamUserBaseUrl;

    private String userToken;

    @Autowired
    private ServiceAuthTokenGenerator tokenGenerator;

    private final ObjectMapper objectMapper;

    private String idamCreateUrl() {
        return idamUserBaseUrl + "/testing-support/accounts";
    }

    private static final String JWT_KEY = "jwtKey";

    public String generateServiceToken() {
        return tokenGenerator.generate();
    }


    public String getUserToken() {
        String clientToken = generateClientToken();
        this.userToken = clientToken;

        return this.userToken;
    }

    public String getUserId() {
        String clientToken = this.userToken;

        String withoutSignature = clientToken.substring(0, clientToken.lastIndexOf('.') + 1);
        Claims claims = Jwts.parser().setSigningKey(JWT_KEY).build().parseSignedClaims(withoutSignature).getPayload();

        return claims.get("id", String.class);
    }


    private String generateClientToken() {
        String code = generateClientCode();
        String token = "";

        String path = idamUserBaseUrl + "/oauth2/token?code=" + code
            + "&client_secret=" + secret
            + "&client_id=" + clientId
            + "&redirect_uri=" + redirectUri
            + "&grant_type=authorization_code";
        ResponseBody body = RestAssured.given().post(path)
            .body();
        String jsonResponse = body.asString();



        try {
            token = objectMapper.readValue(jsonResponse, ClientAuthorizationResponse.class).accessToken;
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        return token;
    }

    private String generateClientCode() {
        String code = "";
        final String encoded = Base64.getEncoder().encodeToString(("testABC@TEST.COM:Probate123").getBytes());

        String jsonResponse = given()
            .relaxedHTTPSValidation()
            .header("Authorization", "Basic " + encoded)
            .post(idamUserBaseUrl + "/oauth2/authorize?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri)
            .asString();

        try {
            code = objectMapper.readValue(jsonResponse, ClientAuthorizationCodeResponse.class).code;
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        return code;

    }

    public void createNewUser() {
        given().headers("Content-type", "application/json")
            .relaxedHTTPSValidation()
            .body(
                "{ \"email\":\"testABC@TEST.COM\", \"forename\":\"testABC@TEST.COM\",\"surname\":\"testABC@TEST.COM\","
                    + "\"password\":\"Probate123\",\"continue-url\":\"test\", \"user_group_name\":\"citizen\"}")
            .post(idamUserBaseUrl + "/testing-support/accounts");
    }
}
