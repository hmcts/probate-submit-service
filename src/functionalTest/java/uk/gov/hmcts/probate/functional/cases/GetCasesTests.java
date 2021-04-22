package uk.gov.hmcts.probate.functional.cases;

import io.restassured.RestAssured;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.probate.functional.IntegrationTestBase;
import uk.gov.hmcts.probate.functional.TestRetryRule;

import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static uk.gov.hmcts.reform.probate.model.cases.CaseType.CAVEAT;
import static uk.gov.hmcts.reform.probate.model.cases.CaseType.GRANT_OF_REPRESENTATION;
import static uk.gov.hmcts.reform.probate.model.cases.CaseType.STANDING_SEARCH;
import static uk.gov.hmcts.reform.probate.model.cases.CaseType.WILL_LODGEMENT;

@RunWith(SpringIntegrationSerenityRunner.class)
public class GetCasesTests extends IntegrationTestBase {

    public static final String INVITE_ID_PLACEHOLDER = "inviteId";
    @Rule
    public TestRetryRule retryRule = new TestRetryRule(3);
    String caseId1;
    String caseId2;
    String inviteId;
    String caveatCaseId;
    @Value("${idam.citizen.username}")
    private String email;
    private Boolean setUp = false;

    @Before
    public void init() {
        if (!setUp) {
            String caseData = utils.getJsonFromFile("gop.singleExecutor.full.json");
            caseId1 = utils.createTestCase(caseData);
            caseData = caseData.replace("gop", "intestacy");
            caseId2 = utils.createTestCase(caseData);

            inviteId = randomAlphanumeric(12).toLowerCase();
            String caveatCaseData = utils.getJsonFromFile("caveat.full.json");
            caveatCaseId = utils.createTestCase(caveatCaseData);

            setUp = true;
        }
    }

    @Test
    public void getCaseByIdAsPathVariableReturns200() {
        RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(utils.getCitizenHeaders())
            .queryParam("caseType", GRANT_OF_REPRESENTATION)
            .when()
            .get("/cases/" + caseId1)
            .then()
            .assertThat()
            .statusCode(200)
            .body("caseData", notNullValue())
            .body("caseInfo.caseId", notNullValue())
            .body("caseInfo.state", equalTo("Pending"))
            .extract().jsonPath().prettify();
    }

    @Test
    public void getCaseByIdAsPathVariableReturns404() {
        RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(utils.getCitizenHeaders())
            .queryParam("caseType", CAVEAT)
            .when()
            .get("/cases/" + caseId1)
            .then()
            .assertThat().statusCode(404);
    }

    @Test
    public void getCaseMissingCaseTypeReturns400() {
        RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(utils.getCitizenHeaders())
            .when()
            .get("/cases/" + caseId1)
            .then()
            .assertThat()
            .statusCode(400);
    }

    @Test
    public void getCaseByIncorrectIdAsPathVariableReturns404() {
        String randomCaseId = RandomStringUtils.randomNumeric(16).toLowerCase();

        RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(utils.getCitizenHeaders())
            .queryParam("caseType", GRANT_OF_REPRESENTATION)
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
            .headers(utils.getCitizenHeaders())
            .queryParam("caseType", GRANT_OF_REPRESENTATION)
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
    public void getCaseByApplicantInvalidEmailReturns404() {
        RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(utils.getCitizenHeaders())
            .queryParam("caseType", GRANT_OF_REPRESENTATION)
            .when()
            .get("/cases/applicantEmail/" + "inValidEmail@test.com")
            .then()
            .assertThat()
            .statusCode(404);
    }

    @Test
    public void getCaseByApplicantEmailMissingCaseTypeReturn400() {
        RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(utils.getCitizenHeaders())
            .when()
            .get("/cases/applicantEmail/" + email)
            .then()
            .assertThat()
            .statusCode(400);
    }

    @Test
    public void getCaseByIncorrectApplicantEmailReturns404() {
        String randomEmail = randomAlphanumeric(5).toLowerCase() + "@email.com";

        RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(utils.getCitizenHeaders())
            .queryParam("caseType", GRANT_OF_REPRESENTATION)
            .when()
            .get("/cases/applicantEmail/" + randomEmail)
            .then()
            .assertThat()
            .statusCode(404)
            .extract().jsonPath().prettify();
    }

    @Test
    public void getAllGrantOfRepresentationCaseTypeReturns200() {
        RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(utils.getCitizenHeaders())
            .queryParam("caseType", GRANT_OF_REPRESENTATION)
            .when()
            .get("/cases/all")
            .then()
            .assertThat()
            .statusCode(200)
            .body("[0].caseData.type", equalTo("GrantOfRepresentation"));

    }

    @Test
    public void getAllStandingSearchGopCasesReturnsEmptyBodyAndStatusCodeIs200() {
        RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(utils.getCitizenHeaders())
            .queryParam("caseType", STANDING_SEARCH)
            .when()
            .get("/cases/all")
            .then()
            .assertThat()
            .statusCode(200)
            .body("isEmpty()", is(true));

    }

    @Test
    public void getAllCaveatGopCasesReturns200() {
        RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(utils.getCitizenHeaders())
            .queryParam("caseType", CAVEAT)
            .when()
            .get("/cases/all")
            .then()
            .assertThat()
            .statusCode(200)
            .log().all(true)
            .body("[0].caseData.type", equalTo("Caveat"));
    }

    @Test
    public void getAllWillLodgementGopCasesReturns200() {
        RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(utils.getCitizenHeaders())
            .queryParam("caseType", WILL_LODGEMENT)
            .when()
            .get("/cases/all")
            .then()
            .assertThat()
            .statusCode(200)
            .extract().jsonPath().prettify();
    }

    @Test
    public void getAllGopCasesInvalidCaseTypeReturns400() {
        RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(utils.getCitizenHeaders())
            .queryParam("caseType", "INVALID_CASE_TYPE")
            .when()
            .get("/cases/all")
            .then()
            .assertThat()
            .statusCode(400);

    }

    @Test
    public void getAllGopCasesMissingCaseTypeReturns400() {
        RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(utils.getCitizenHeaders())
            .when()
            .get("/cases/all")
            .then()
            .assertThat()
            .statusCode(400);
    }

    @Test
    public void getCaseByInviteIdReturns200() {
        if (retryRule.firstAttempt) {
            String inviteCaseData = utils.getJsonFromFile("gop.multipleExecutors.full.json");
            inviteCaseData = inviteCaseData.replace(INVITE_ID_PLACEHOLDER, inviteId);
            utils.createTestCase(inviteCaseData);
        }

        RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(utils.getCitizenHeaders())
            .queryParam("caseType", GRANT_OF_REPRESENTATION)
            .when()
            .get("/cases/invitation/" + inviteId)
            .then()
            .assertThat()
            .statusCode(200)
            .body("caseData", notNullValue())
            .body("caseInfo.caseId", notNullValue())
            .body("caseInfo.state", equalTo("Pending"))
            .extract().jsonPath().prettify();
    }

    @Test
    public void getCaseByInviteIdAndInCorrectHeaderReturns404() {

        RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(utils.getCaseworkerHeaders())
            .queryParam("caseType", GRANT_OF_REPRESENTATION)
            .when()
            .get("/cases/invitation/" + inviteId)
            .then()
            .statusCode(404);
    }

    @Test
    public void getCaseByIncorrectInviteIdReturns404() {
        String randomInviteId = randomAlphanumeric(12).toLowerCase();

        RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(utils.getCitizenHeaders())
            .queryParam("caseType", GRANT_OF_REPRESENTATION)
            .when()
            .get("/cases/invitation/" + randomInviteId)
            .then()
            .assertThat()
            .statusCode(404);
    }

    @Test
    public void getCaseByCaseIdAsRequestParamReturns200() {
        RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(utils.getCitizenHeaders())
            .queryParam("caseId", caseId1)
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
    public void getCaseByCaseIdForCaseTypeIntestacyAsRequestParamReturns200() {
        RestAssured.given()
            .relaxedHTTPSValidation()
            .headers(utils.getCitizenHeaders())
            .queryParam("caseId", caseId2)
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
            .headers(utils.getCitizenHeaders())
            .queryParam("caseId", randomCaseId)
            .when()
            .get("/cases")
            .then()
            .assertThat()
            .statusCode(400)
            .extract().jsonPath().prettify();
    }
}