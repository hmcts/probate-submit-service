package uk.gov.hmcts.probate.functional.cases;

import io.restassured.RestAssured;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.probate.functional.IntegrationTestBase;
import uk.gov.hmcts.reform.probate.model.cases.CaseType;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SpringIntegrationSerenityRunner.class)
public class ValidateCasesTests extends IntegrationTestBase {

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
    public void validateCaseReturns200() {
        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .queryParam("caseType", CaseType.GRANT_OF_REPRESENTATION)
                .when()
                .put("/cases/" + testCaseId + "/validations")
                .then()
                .assertThat()
                .statusCode(200)
                .body("caseData", notNullValue())
                .body("caseInfo.caseId", notNullValue())
                .body("caseInfo.state", equalTo("Pending"))
                .extract().jsonPath().prettify();
    }

    @Test
    public void validateCaseIncorrectIdReturns404() {
        String randomCaseId = RandomStringUtils.randomNumeric(16).toLowerCase();

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .queryParam("caseType", CaseType.GRANT_OF_REPRESENTATION)
                .when()
                .put("/cases/" + randomCaseId + "/validations")
                .then()
                .assertThat()
                .statusCode(404);
    }

    @Test
    public void validateCaseWithInvalidDataReturns400() throws InterruptedException {
        String invalidCaseData = utils.getJsonFromFile("failure.validateCaseData.json");
        String invalidCaseId = utils.createTestCase(invalidCaseData);

        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .queryParam("caseType", CaseType.GRANT_OF_REPRESENTATION)
                .when()
                .put("/cases/" + invalidCaseId + "/validations")
                .then()
                .assertThat()
                .statusCode(400)
                .extract().jsonPath().prettify();
    }

    @Test
    public void validateCaseWithMissingCaseTypeReturns400() {
        RestAssured.given()
                .relaxedHTTPSValidation()
                .headers(utils.getHeaders())
                .when()
                .put("/cases/" + testCaseId + "/validations")
                .then()
                .assertThat()
                .statusCode(400);
    }
}
