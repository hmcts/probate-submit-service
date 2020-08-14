package uk.gov.hmcts.probate.functional.cases;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.probate.functional.IntegrationTestBase;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;

@RunWith(SpringIntegrationSerenityRunner.class)
public class GetCasesTests extends IntegrationTestBase {

    @Value("${idam.username}")
    private String email;

    private Boolean setUp = false;
    private static final String EMAIL_PLACEHOLDER = "XXXXXXXXXX";

    private String caseId;

    @Before
    public void init() {
        if (!setUp) {
            String caseData = utils.getJsonFromFile("intestacyGrantOfRepresentation_partial_draft.json");

            caseData = caseData.replace(EMAIL_PLACEHOLDER, email);
            Response response = RestAssured.given()
                    .relaxedHTTPSValidation()
                    .headers(utils.getHeaders())
                    .body(caseData)
                    .when()
                    .post("/cases/initiate");
            JsonPath jsonPath = JsonPath.from(response.getBody().asString());
            caseId = jsonPath.get("caseInfo.caseId");

            setUp = true;
        }
    }

    @Test
    public void getCaseByIdAsPathVariableReturns200() {
        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .queryParam("caseType",CaseType.GRANT_OF_REPRESENTATION)
                .when()
                .get("/cases/" + caseId)
                .then()
                .assertThat()
                .statusCode(200)
                .extract().jsonPath().prettify();
    }

    @Test
    public void getCaseByIncorrectIdAsPathVariableReturns404() {
        String randomCaseId = RandomStringUtils.randomNumeric(16).toLowerCase();

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .queryParam("caseType",CaseType.GRANT_OF_REPRESENTATION)
                .when()
                .get("/cases/" + randomCaseId)
                .then()
                .assertThat()
                .statusCode(404)
                .extract().jsonPath().prettify();
    }

    @Test
    public void getCaseByApplicantEmailReturns200() {
        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .queryParam("caseType",CaseType.GRANT_OF_REPRESENTATION)
                .when()
                .get("/cases/applicantEmail/" + email)
                .then()
                .assertThat()
                .statusCode(200)
                .extract().jsonPath().prettify();
    }

    @Test
    public void getCaseByIncorrectApplicantEmailReturns404() {
        String randomEmail = RandomStringUtils.randomAlphanumeric(5).toLowerCase() + "@email.com";

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .queryParam("caseType",CaseType.GRANT_OF_REPRESENTATION)
                .when()
                .get("/cases/applicantEmail/" + randomEmail)
                .then()
                .assertThat()
                .statusCode(404)
                .extract().jsonPath().prettify();
    }

    @Test
    public void getAllGOPCasesReturns200() {
        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .queryParam("caseType",CaseType.GRANT_OF_REPRESENTATION)
                .when()
                .get("/cases/all")
                .then()
                .assertThat()
                .statusCode(200)
                .extract().jsonPath().prettify();
    }

    @Test
    public void getCaseByInviteIdReturns200 () {
//        RestAssured.given()
//                .relaxedHTTPSValidation()
//                .headers(utils.getHeaders())
//                .queryParam("caseType",CaseType.GRANT_OF_REPRESENTATION)
//                .when()
//                .get("/cases/invitation/" + inviteId)
//                .then()
//                .assertThat()
//                .statusCode(200)
//                .extract().jsonPath().prettify();
    }

    @Test
    public void getCaseByIncorrectInviteIdReturns404 () {
//        RestAssured.given()
//                .relaxedHTTPSValidation()
//                .headers(utils.getHeaders())
//                .queryParam("caseType",CaseType.GRANT_OF_REPRESENTATION)
//                .when()
//                .get("/cases/invitation/" + inviteId)
//                .then()
//                .assertThat()
//                .statusCode(404)
//                .extract().jsonPath().prettify();
    }

    @Test
    public void getCaseByCaseIdAsRequestParamReturns200() {
        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .queryParam("caseId", caseId)
                .when()
                .get("/cases")
                .then()
                .assertThat()
                .statusCode(200)
                .extract().jsonPath().prettify();
    }

    @Test
    public void getCaseByIncorrectIdAsRequestParamReturns400() {
        String randomCaseId = RandomStringUtils.randomNumeric(16).toLowerCase();

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .queryParam("caseId", randomCaseId)
                .when()
                .get("/cases")
                .then()
                .assertThat()
                .statusCode(400)
                .extract().jsonPath().prettify();
    }
}
