package uk.gov.hmcts.probate.functional.cases;

import io.restassured.RestAssured;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.probate.functional.IntegrationTestBase;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SpringIntegrationSerenityRunner.class)
public class GetCasesTests extends IntegrationTestBase {

    @Value("${idam.username}")
    private String email;

    private Boolean setUp = false;

    String testCaseId;

    @Before
    public void init() throws InterruptedException {
        if (!setUp) {
            String caseData = utils.getJsonFromFile("success.saveCaseData.json");
            testCaseId = utils.createTestCase(caseData);

            setUp = true;
        }
    }

    @Test
    public void getCaseByIdAsPathVariableReturns200() {
        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .queryParam("caseType", CaseType.GRANT_OF_REPRESENTATION)
                .when()
                .get("/cases/" + testCaseId)
                .then()
                .assertThat()
                .statusCode(200)
                .body("caseData", notNullValue())
                .body("caseInfo.caseId", notNullValue())
                .body("caseInfo.state", equalTo("Pending"))
                .extract().jsonPath().prettify();
    }

    @Test
    public void getCaseMissingCaseTypeReturns400() {
        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .when()
                .get("/cases/" + testCaseId)
                .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    public void getCaseByIncorrectIdAsPathVariableReturns404() {
        String randomCaseId = RandomStringUtils.randomNumeric(16).toLowerCase();

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .queryParam("caseType", CaseType.GRANT_OF_REPRESENTATION)
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
                .queryParam("caseType", CaseType.GRANT_OF_REPRESENTATION)
                .when()
                .get("/cases/applicantEmail/" + email)
                .then()
                .assertThat()
                .statusCode(200)
                .body("caseData", notNullValue())
                .body("caseInfo.caseId", notNullValue())
                .body("caseInfo.state", equalTo("Pending"))
                .extract().jsonPath().prettify();
    }

    @Test
    public void getCaseByApplicantEmailMissingCaseTypeReturn400() {
        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .when()
                .get("/cases/applicantEmail/" + email)
                .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    public void getCaseByIncorrectApplicantEmailReturns404() {
        String randomEmail = RandomStringUtils.randomAlphanumeric(5).toLowerCase() + "@email.com";

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .queryParam("caseType", CaseType.GRANT_OF_REPRESENTATION)
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
                .queryParam("caseType", CaseType.GRANT_OF_REPRESENTATION)
                .when()
                .get("/cases/all")
                .then()
                .assertThat()
                .statusCode(200)
                .extract().jsonPath().prettify();
    }

    @Test
    public void getAllGOPCasesMissingCaseTypeReturns400() {
        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .when()
                .get("/cases/all")
                .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    public void getCaseByInviteIdReturns200() {
    }

    @Test
    public void getCaseByIncorrectInviteIdReturns404() {
    }

    @Test
    public void getCaseByCaseIdAsRequestParamReturns200() {
        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .queryParam("caseId", testCaseId)
                .when()
                .get("/cases")
                .then()
                .assertThat()
                .statusCode(200)
                .body("caseData", notNullValue())
                .body("caseInfo.caseId", notNullValue())
                .body("caseInfo.state", equalTo("Pending"))
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
