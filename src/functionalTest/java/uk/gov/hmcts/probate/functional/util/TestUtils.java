package uk.gov.hmcts.probate.functional.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.probate.functional.TestContextConfiguration;
import uk.gov.hmcts.probate.functional.TestTokenGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import jakarta.annotation.PostConstruct;

@ContextConfiguration(classes = TestContextConfiguration.class)
@Component
public class TestUtils {

    public static final String APPLICATION_ID = "appId";
    public static final String EMAIL_PLACEHOLDER = "testusername@test.com";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String AUTHORIZATION = "Authorization";
    public static final String CITIZEN = "citizen";
    @Value("${idam.citizen.username}")
    public String citizenEmail;
    @Value("${probate.submit.url}")
    public String submitServiceUrl;
    @Autowired
    protected TestTokenGenerator testTokenGenerator;
    @Value("${idam.caseworker.username}")
    private String caseworkerEmail;
    private String serviceToken;

    @PostConstruct
    public void init() throws JsonProcessingException {
        serviceToken = testTokenGenerator.generateServiceAuthorisation();

        testTokenGenerator.createNewUser(citizenEmail, CITIZEN);

        RestAssured.baseURI = submitServiceUrl;
    }

    public String getJsonFromFile(String fileName) {
        try {
            File file = ResourceUtils.getFile(this.getClass().getResource("/json/" + fileName));
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String createTestCase(String caseData) {
        caseData = caseData.replace(EMAIL_PLACEHOLDER, citizenEmail);

        JsonPath jsonPath  = RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(getCitizenHeaders())
            .body(caseData)
            .when()
            .post("/cases/initiate")
            .then()
            .assertThat()
            .statusCode(200)
            .extract().jsonPath();
        return jsonPath.get("caseInfo.caseId");
    }

    public String createCaveatTestCase(String caseData) {
        String applicationId = RandomStringUtils.randomNumeric(16).toLowerCase();
        caseData = caseData.replace(APPLICATION_ID, applicationId);

        JsonPath jsonPath = RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(getCitizenHeaders())
            .body(caseData)
            .when()
            .post("/submissions/" + applicationId)
            .then()
            .assertThat()
            .statusCode(200)
            .extract().jsonPath();
        return jsonPath.get("probateCaseDetails.caseInfo.caseId");
    }

    public Headers getCitizenHeaders() {
        return getHeaders(citizenEmail);
    }

    public Headers getCaseworkerHeaders() {
        return getHeaders(caseworkerEmail);
    }

    public Headers getHeaders(String email) {
        return Headers.headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header(CONTENT_TYPE, ContentType.JSON.toString()),
            new Header(AUTHORIZATION, testTokenGenerator.generateAuthorisation(email)));
    }
}